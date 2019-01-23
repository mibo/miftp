package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.boundary.FileEndpoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class FtpHandler @Autowired constructor(private val server: MiFtpServer) {

  fun listFiles(user: String):List<FileEndpoint> {
    val view = server.getFileSystemView(user)
    return view.homeDirectory.listFiles().map { FileEndpoint.create(it) }
  }

  fun getFileById(user: String, id: String): Optional<FileEndpoint> {
    println("Request file $id for user $user")
    val view = server.getFileSystemView(user)
    // FIXME: check if/how inefficient this is
//    return view.homeDirectory.listFiles()
//            .map { FileEndpoint.create(it) }
//            .firstOrNull { it.name == id } ?: return null
    return Optional.ofNullable(view.homeDirectory.listFiles().firstOrNull { it.name == id })
              .map { FileEndpoint.create(it) }
  }

  fun getFileByPath(user: String, path: String): Optional<FileEndpoint> {
    println("Request file by path=$path for user $user")
    val view = server.getFileSystemView(user)
    view.changeWorkingDirectory("/")
//    val paths = path.split("/")
//    view.changeWorkingDirectory()
    val file = view.getFile(path)
    return Optional.ofNullable(if(file.isFile) file else null).map { FileEndpoint.create(it) }
  }

  fun getFileContentById(user: String, id: String): ByteBuffer? {
    val view = getFileById(user, id).map { it.content() }
    // FIXME: check if/how inefficient this is
    return view.orElse(null)
  }
//  fun list
}

