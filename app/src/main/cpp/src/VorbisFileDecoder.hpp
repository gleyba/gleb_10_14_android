#pragma once

#include <string>
#include <vorbis/codec.h>
#include <functional>
#include <iostream>
#include <fstream>



class VorbisFileDecoder final {

    static constexpr int BUFFER_LENGTH = 4096;

    std::ifstream _file;

    const long _channels;
    const long _sampleRate;

    ogg_sync_state   oy; /* sync and verify incoming physical bitstream */
    ogg_stream_state os; /* take physical pages, weld into a logical stream of packets */
    ogg_page         og; /* one Ogg bitstream page. Vorbis packets are inside */
    ogg_packet       op; /* one raw packet of data for decode */

    vorbis_info      vi; /* struct that stores all the static vorbis bitstream settings */
    vorbis_comment   vc; /* struct that stores all the bitstream user comments */
    vorbis_dsp_state vd; /* central working state for the packet->PCM decoder */
    vorbis_block     vb; /* local working space for packet->PCM decode */

    std::function<void(float)> _soundEnergyLevelListener;

    int _convSize = BUFFER_LENGTH;

public:

    VorbisFileDecoder(
        std::string fileName,
        long channels,
        long sampleRate
    );

    bool initialize();

    void setSoundEnergyLevelListener(std::function<void(float)> listener);

    bool readPcm(ogg_int16_t* buffer, std::function<bool(int size)> acceptor);

    void deInitialize();

    ~VorbisFileDecoder();

};
