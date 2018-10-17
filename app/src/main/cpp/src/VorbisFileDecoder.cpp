#include "VorbisFileDecoder.hpp"

#include <cassert>
#include <cstdlib>
#include <time.h>

#include "platform.hpp"

VorbisFileDecoder::VorbisFileDecoder(
    std::string fileName,
    long channels,
    long sampleRate
)
: _file{std::move(fileName)}
, _channels{channels}
, _sampleRate{sampleRate}
{}

bool VorbisFileDecoder::initialize() {
    if (!_file.is_open()) {
        return false;
    }

    return true;
}

void VorbisFileDecoder::setSoundEnergyLevelListener(std::function<void(long)> listener) {
    _soundEnergyLevelListener = listener;
}

int VorbisFileDecoder::readPcm(const ogg_int16_t* buffer, int size) {

    return 0;
}

void VorbisFileDecoder::deInitialize() {

    if (_file.is_open()) {
        _file.close();
    }
}


VorbisFileDecoder::~VorbisFileDecoder() {
    deInitialize();
}