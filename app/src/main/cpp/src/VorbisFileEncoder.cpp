#include "VorbisFileEncoder.hpp"

#include <cassert>
#include <cstdlib>
#include <time.h>

#include "platform.hpp"


VorbisFileEncoder::VorbisFileEncoder(
    std::string fileName,
    long channels,
    long sampleRate,
    float quality
)
: _file{std::move(fileName)}
, _channels{channels}
, _sampleRate{sampleRate}
, _quality{quality}
{}

bool VorbisFileEncoder::initialize() {
    if (!_file.is_open()) {
        return false;
    }

    vorbis_info_init(&vi);

    /*
     * Encoding using a VBR quality mode.  The usable range is -.1
     * (lowest quality, smallest file) to 1. (highest quality, largest file).
     * Example quality mode .4: 44kHz stereo coupled, roughly 128kbps VBR
     */
    if (vorbis_encode_init_vbr(
        &vi,
        _channels,
        _sampleRate,
        _quality
    ) != 0) {
        return false;
    }

    /* add a comment */
    logstr("VorbisEncoder: Adding comments");
    vorbis_comment_init(&vc);
    vorbis_comment_add_tag(&vc,"ENCODER","JNIVorbisEncoder");

    /* set up the analysis state and auxiliary encoding storage */
    vorbis_analysis_init(&vd,&vi);
    vorbis_block_init(&vd,&vb);

    /* set up our packet->stream encoder */
    /* pick a random serial number; that way we can more likely build
       chained streams just by concatenation */
    srand(time(NULL));
    ogg_stream_init(&os,rand());

    /* Vorbis streams begin with three headers; the initial header (with
       most of the codec setup parameters) which is mandated by the Ogg
       bitstream spec.  The second header holds any comment fields.  The
       third header holds the bitstream codebook.  We merely need to
       make the headers, then pass them to libvorbis one at a time;
       libvorbis handles the additional Ogg bitstream constraints */

    {
        ogg_packet header;
        ogg_packet header_comm;
        ogg_packet header_code;

        vorbis_analysis_headerout(&vd,&vc,&header,&header_comm,&header_code);
        ogg_stream_packetin(&os,&header); /* automatically placed in its own
                                           page */
        ogg_stream_packetin(&os,&header_comm);
        ogg_stream_packetin(&os,&header_code);

        /* This ensures the actual
         * audio data will start on a new page, as per spec
         */
        logstr("VorbisEncoder : Writting header");
        while(ogg_stream_flush(&os,&og) != 0){
            _file.write((const char*) og.header, og.header_len);
            _file.write((const char*) og.body, og.body_len);
        }

    }

    return true;
}

void VorbisFileEncoder::setSoundEnergyLevelListener(std::function<void(long)> listener) {
    _soundEnergyLevelListener = listener;
}

void VorbisFileEncoder::writePcm(const char* readbuffer, long size) {
//    logstr("VorbisFileEncoder::writePcm size:" + std::to_string(size));
    long i;
    /* data to encode */

    /* expose the buffer to submit data */
    float **buffer=vorbis_analysis_buffer(&vd,size/(2*_channels));

    /* uninterleave samples */
    int channel;
    for(i=0;i<size/(2*_channels);i++) {
        for(channel = 0; channel < _channels; channel++) {
            buffer[channel][i]=((readbuffer[i*(2*_channels)+(channel*2+1)]<<8)|
                                (0x00ff&(int)readbuffer[i*(2*_channels)+(channel*2)]))/32768.f;
        }
    }

    if (_soundEnergyLevelListener) {
        long sumOfSampleSquares = 0;
        for(i=0;i<size;i++) {
            long sample = readbuffer[i];
            sumOfSampleSquares += sample * sample;
        }
        long soundEnergy = sumOfSampleSquares/size;
//        logstr("VorbisFileEncoder::writePcm sumOfSampleSquares:" + std::to_string(sumOfSampleSquares));
        _soundEnergyLevelListener(soundEnergy);
    }

    /* tell the library how much we actually submitted */
    vorbis_analysis_wrote(&vd,i);

    int eos=0;
    /* vorbis does some data preanalysis, then divvies up blocks for
         more involved (potentially parallel) processing.  Get a single
         block for encoding now */
    while(vorbis_analysis_blockout(&vd,&vb)==1){

        /* analysis, assume we want to use bitrate management */
        vorbis_analysis(&vb,NULL);
        vorbis_bitrate_addblock(&vb);

        while(vorbis_bitrate_flushpacket(&vd,&op)){

            /* weld the packet into the bitstream */
            ogg_stream_packetin(&os,&op);

            /* write out pages (if any) */
            while(!eos){
                int result=ogg_stream_pageout(&os,&og);
                if(result==0)
                    break;

                _file.write((const char*) og.header, og.header_len);
                _file.write((const char*) og.body, og.body_len);

                /* this could be set above, but for illustrative purposes, I do
                   it here (to show that vorbis does know where the stream ends) */

                if(ogg_page_eos(&og))
                    eos=1;
            }
        }
    }
}

void VorbisFileEncoder::deInitialize() {

    /* end of file.  this can be done implicitly in the mainline,
           but it's easier to see here in non-clever fashion.
           Tell the library we're at end of stream so that it can handle
           the last frame and mark end of stream in the output properly */
    logstr("VorbisEncoder : End of file");
    vorbis_analysis_wrote(&vd,0);
    if (_file.is_open()) {
        _file.close();
    }
    ogg_stream_clear(&os);
    vorbis_block_clear(&vb);
    vorbis_dsp_clear(&vd);
    vorbis_comment_clear(&vc);
    vorbis_info_clear(&vi);
}


VorbisFileEncoder::~VorbisFileEncoder() {
    deInitialize();
}