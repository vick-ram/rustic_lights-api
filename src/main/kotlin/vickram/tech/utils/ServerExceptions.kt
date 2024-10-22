package vickram.tech.utils

class BlankException(override val message: String?): Exception(message)
class NotFoundException(override val message: String?): Exception(message)
class BadRequestException(override val message: String?): Exception(message)
class UnauthorizedException(override val message: String?): Exception(message)
class InvalidInputException(override val message: String?): Exception(message)