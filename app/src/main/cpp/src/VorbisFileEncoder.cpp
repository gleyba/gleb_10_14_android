#include "VorbisFileEncoder.hpp"

VorbisFileEncoder::VorbisFileEncoder(
    std::string fileName
)
: _fileName{std::move(fileName)}
{}