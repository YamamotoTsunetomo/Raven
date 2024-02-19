package com.tsunetomo.raven.model

import java.io.IOException

class NetworkException : IOException()
class DownloadLinkJobDoneException(val link: String) : Exception()
class InvalidDownloadPageException : Exception()
