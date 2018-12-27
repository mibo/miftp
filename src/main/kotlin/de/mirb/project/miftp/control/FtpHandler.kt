package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.boundary.FileView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class FtpHandler @Autowired constructor(private val server: MiFtpServer) {

  fun listFiles(user: String):List<FileView> {
    val view = server.getFileSystemView(user)
    return view.homeDirectory.listFiles().map { FileView.create(it) }
  }

  fun getFileById(user: String, id: String): Optional<FileView> {
    println("Request file $id for user $user")
    val view = server.getFileSystemView(user)
    // FIXME: check if/how inefficient this is
//    return view.homeDirectory.listFiles()
//            .map { FileView.create(it) }
//            .firstOrNull { it.name == id } ?: return null
    return Optional.ofNullable(view.homeDirectory.listFiles().firstOrNull { it.name == id })
              .map { FileView.create(it) }
  }

  fun getFileContentById(user: String, id: String): ByteBuffer? {
    val view = getFileById(user, id).map { it.content() }
    // FIXME: check if/how inefficient this is
    return view.orElse(null)
  }
//  fun list
}

