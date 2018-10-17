#include "VorbisFileDecoder.hpp"

#include <cassert>
#include <cstdlib>
#include <cmath>
#include <time.h>

#include "platform.hpp"



VorbisFileDecoder::VorbisFileDecoder(
    std::string fileName,
    long channels,
    long sampleRate
)
: _file{std::move(fileName),std::ifstream::in}
, _channels{channels}
, _sampleRate{sampleRate}
{}

bool VorbisFileDecoder::initialize() {
    if (!_file.is_open() || !_file.good()) {
        return false;
    }

    ogg_sync_init(&oy); /* Now we can read pages */

    int  bytes = BUFFER_LENGTH;
    char* buffer=ogg_sync_buffer(&oy,BUFFER_LENGTH);

    _file.read(buffer,BUFFER_LENGTH);
    ogg_sync_wrote(&oy,bytes);

    /* Get the first page. */
    logstr("VorbisDecoder: Getting the first page, read (%d) bytes:" + std::to_string(bytes));
    if(ogg_sync_pageout(&oy,&og)!=1){

        /* error case.  Must not be Vorbis data */
        return false;
    }

    logstr("VorbisDecoder: Successfully fetched the first page");

    /* Get the serial number and set up the rest of decode. */
    /* serialno first; use it to set up a logical stream */
    ogg_stream_init(&os,ogg_page_serialno(&og));

    /* extract the initial header from the first page and verify that the
    Ogg bitstream is in fact Vorbis data */

    /* I handle the initial header first instead of just having the code
    read all three Vorbis headers at once because reading the initial
    header is an easy way to identify a Vorbis bitstream and it's
    useful to see that functionality seperated out. */

    vorbis_info_init(&vi);
    vorbis_comment_init(&vc);
    if(ogg_stream_pagein(&os,&og)<0){
        /* error; stream version mismatch perhaps */
        return false;
    }


    if(ogg_stream_packetout(&os,&op)!=1){
        /* no page? must not be vorbis */
        return false;
    }


    if(vorbis_synthesis_headerin(&vi,&vc,&op)<0){
        /* error case; not a vorbis header */
        return false;
    }


    /* At this point, we're sure we're Vorbis. We've set up the logical
    (Ogg) bitstream decoder. Get the comment and codebook headers and
    set up the Vorbis decoder */

    /* The next two packets in order are the comment and codebook headers.
    They're likely large and may span multiple pages. Thus we read
    and submit data until we get our two packets, watching that no
    pages are missing. If a page is missing, error out; losing a
    header page is the only place where missing data is fatal. */

    int i=0;
    while(i<2){
        while(i<2){
            int result=ogg_sync_pageout(&oy,&og);
            if(result==0)break; /* Need more data */
            /* Don't complain about missing or corrupt data yet. We'll
            catch it at the packet output phase */
            if(result==1){
                ogg_stream_pagein(&os,&og); /* we can ignore any errors here
                    as they'll also become apparent
                    at packetout */
                while(i<2){
                    result=ogg_stream_packetout(&os,&op);
                    if(result==0)break;
                    if(result<0){
                        /* Uh oh; data at some point was corrupted or missing!
                        We can't tolerate that in a header.  Die. */
                        return false;
                    }
                    result=vorbis_synthesis_headerin(&vi,&vc,&op);
                    if(result<0){
                        return false;
                    }
                    i++;
                }
            }
        }
        /* no harm in not checking before adding more */
        buffer=ogg_sync_buffer(&oy,BUFFER_LENGTH);
        _file.read(buffer,BUFFER_LENGTH);
        if(!_file.good()){
            return false;
        }
        ogg_sync_wrote(&oy,bytes);
    }


    /* Throw the comments plus a few lines about the bitstream we're
    decoding */
    {
        char **ptr=vc.user_comments;
        while(*ptr){
            ++ptr;
        }

        logstr("VorbisDecoder: Bitstream is " + std::to_string(vi.channels) + " channel");
        logstr("VorbisDecoder: Bitstream " + std::to_string(vi.rate) + " Hz");
        logstr("VorbisDecoder: Encoded by: " + std::string(vc.vendor));
    }

    _convSize = BUFFER_LENGTH/vi.channels;

    /* OK, got and parsed all three headers. Initialize the Vorbis
    packet->PCM decoder. */
    if(vorbis_synthesis_init(&vd,&vi)!=0){
        logstr("VorbisDecoder: Error: Corrupt header during playback initialization.");
        return false;
    }

    /* central decode state */
    vorbis_block_init(&vd,&vb);          /* local state for most of the decode
    so multiple block decodes can
    proceed in parallel. We could init
    multiple vorbis_block structures
    for vd here */

    return true;
}

void VorbisFileDecoder::setSoundEnergyLevelListener(std::function<void(float)> listener) {
    _soundEnergyLevelListener = listener;
}

bool VorbisFileDecoder::readPcm(ogg_int16_t* outBuffer, std::function<bool(int size)> acceptor) {
    if (!_file.good()) {
        return false;
    }

    while (ogg_sync_pageout(&oy,&og) <= 0) {
        char * buffer = ogg_sync_buffer(&oy,BUFFER_LENGTH);
        _file.read(buffer,BUFFER_LENGTH);

        ogg_sync_wrote(&oy,BUFFER_LENGTH);
    }

    ogg_stream_pagein(&os, &og); /* can safely ignore errors at
                        this point */
    while (1) {
        int result = ogg_stream_packetout(&os, &op);

        if (result == 0) {
            break; /* need more data */
        } else if (result < 0) {
            /* missing or corrupt data at this page position */
            /* no reason to complain; already complained above */
            continue;
        }

        /* we have a packet.  Decode it */
        float **pcm;
        int samples;

        if (vorbis_synthesis(&vb, &op) == 0) /* test for success! */
            vorbis_synthesis_blockin(&vd, &vb);
        /*

        **pcm is a multichannel float vector.  In stereo, for
        example, pcm[0] is left, and pcm[1] is right.  samples is
        the size of each channel.  Convert the float values
        (-1.<=range<=1.) to whatever PCM format and write it out */

        while ((samples = vorbis_synthesis_pcmout(&vd, &pcm)) > 0) {
            int j;
            int clipflag = 0;
            int bout = (samples < _convSize ? samples : _convSize);

            float sumOfSampleSquares = 0;

            /* convert floats to 16 bit signed ints (host order) and
            interleave */
            for (int i = 0; i < vi.channels; i++) {
                ogg_int16_t *ptr = outBuffer + i;
                float *mono = pcm[i];
                for (j = 0; j < bout; j++) {

                    float sampleF = mono[j];
#if 1
                    int val = floor(sampleF * 32767.f + .5f);
#else /* optional dither */
                    int val=mono[j]*32767.f+drand48()-0.5f;
#endif

                    sumOfSampleSquares += sampleF * sampleF;

                    /* might as well guard against clipping */
                    if (val > 32767) {
                        val = 32767;
                        clipflag = 1;
                    }

                    if (val < -32768) {
                        val = -32768;
                        clipflag = 1;
                    }

                    *ptr = val;
                    ptr += vi.channels;
                }
            }

            if (_soundEnergyLevelListener) {
                float result = sumOfSampleSquares/(float)(bout * vi.channels);
                _soundEnergyLevelListener(result);
            }

            if (clipflag) {
                logstr("VorbisDecoder: Clipping in frame n" + std::to_string(vd.sequence));
            }

            if (!acceptor(bout * vi.channels)) {
                return false;
            }

            vorbis_synthesis_read(&vd, bout); /* tell libvorbis how many samples we actually consumed */
        }
    }

    if (ogg_page_eos(&og))
        return false;

    return true;
}

void VorbisFileDecoder::deInitialize() {

    _soundEnergyLevelListener = nullptr;

    if (_file.is_open()) {
        _file.close();
    }


    /* ogg_page and ogg_packet structs always point to storage in
    libvorbis.  They're never freed or manipulated directly */
    vorbis_block_clear(&vb);
    vorbis_dsp_clear(&vd);

    ogg_stream_clear(&os);
    vorbis_comment_clear(&vc);
    vorbis_info_clear(&vi);  /* must be called last */
    /* OK, clean up the framer */
    ogg_sync_clear(&oy);
}


VorbisFileDecoder::~VorbisFileDecoder() {
    deInitialize();
}