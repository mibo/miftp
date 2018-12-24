package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.boundary.FileView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FtpHandler @Autowired constructor(private val server: MiFtpServer) {

  fun listFiles(user: String):List<FileView> {
    println("Called list files")
    val view = server.getFileSystemView(user)
    println("Fs view: $view")
    return view.homeDirectory.listFiles().map { FileView(it) }
  }
//  fun list
}

