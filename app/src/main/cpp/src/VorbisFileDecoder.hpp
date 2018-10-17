#pragma once

#include <string>
#include <vorbis/codec.h>
#include <functional>
#include <iostream>
#include <fstream>

class VorbisFileDecoder final {

    std::ifstream _file;

    const long _channels;
    const long _sampleRate;

    std::function<void(long)> _soundEnergyLevelListener;

public:

    VorbisFileDecoder(
        std::string fileName,
        long channels,
        long sampleRate
    );

    bool initialize();

    void setSoundEnergyLevelListener(std::function<void(long)> listener);

    int readPcm(const ogg_int16_t* buffer, int size);

    void deInitialize();

    ~VorbisFileDecoder();

};
