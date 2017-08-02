package tech.sourced.berserker.service

import com.google.protobuf.ByteString
import github.com.srcd.berserker.enrysrv.generated.{EnryRequest, EnrysrvServiceGrpc}
import io.grpc.ManagedChannelBuilder
import org.apache.log4j.Logger

import scala.sys.process._


class EnryService(host: String, port: Int, maxMsgSize: Int) {

  private val log = Logger.getLogger(getClass.getName)

  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .maxInboundMessageSize(maxMsgSize)
    .build()

  private val stub = EnrysrvServiceGrpc.blockingStub(channel)

  def getLanguage(filename: String, content: Array[Byte] = Array.emptyByteArray) = {
    val req = if (content.isEmpty) {
      log.debug(s"Detecting lang for $filename")
      EnryRequest(fileName = filename)
    } else {
      log.debug(s"Detecting lang for $filename by content")
      EnryRequest(fileName = filename, fileContent = ByteString.copyFrom(content))
    }
    val guess = stub.getLanguage(req)
    log.info(s"Detected filename: $filename, lang: ${guess.language}, status: ${guess.status}")
    guess
  }

}

object EnryService {

  def startProcess(enrysrv: String) = {
    val log = Logger.getLogger(getClass.getName)
    log.info(s"Starting Enry server process using $enrysrv")
    val procsss = s"$enrysrv server".run
  }

  def apply(host: String, port: Int, maxMsgSize: Int): EnryService =
    new EnryService(host, port, maxMsgSize)

  def processIsNotRunning(): Boolean = {
    var running = false
    try {
      ("ps aux" #| "grep [e]nrysrv" !!)
      running = true
    }
    catch {
      case _: Throwable => running = false
    }
    val log = Logger.getLogger(getClass.getName)
    log.info("Enry process is not running")
    !running
  }

}
