#pragma once

#include <string>
#include <vorbis/vorbisenc.h>
#include <functional>
#include <iostream>
#include <fstream>

class VorbisFileEncoder final {

    std::ofstream _file;

    const long _channels;
    const long _sampleRate;
    const float _quality;

    ogg_stream_state os; /* take physical pages, weld into a logical
                            stream of packets */
    ogg_page         og; /* one Ogg bitstream page.  Vorbis packets are inside */
    ogg_packet       op; /* one raw packet of data for decode */

    vorbis_info      vi; /* struct that stores all the static vorbis bitstream
                            settings */
    vorbis_comment   vc; /* struct that stores all the user comments */

    vorbis_dsp_state vd; /* central working state for the packet->PCM decoder */
    vorbis_block     vb; /* local working space for packet->PCM decode */

    std::function<void(float)> _soundEnergyLevelListener;

public:

    VorbisFileEncoder(
        std::string fileName,
        long channels,
        long sampleRate,
        float quality
    );

    bool initialize();

    void setSoundEnergyLevelListener(std::function<void(float)> listener);
    void writePcm(const char* buffer, long size);

    void deInitialize();

    ~VorbisFileEncoder();

};