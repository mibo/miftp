package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.boundary.FileView
import org.apache.ftpserver.ftplet.FtpFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FtpHandler @Autowired constructor(private val server: MiFtpServer) {

  fun listFiles(user: String):List<FileView> {
    println("Called list files")
    val view = server.getFileSystemView(user)
    println("Fs view: $view")
    return view.homeDirectory.listFiles().map { FileView.create(it) }
  }

  fun getFileContentById(user: String, id: String): FileView? {
    val view = server.getFileSystemView(user)
    println("Fs view: $view")
//    return view.homeDirectory.listFiles()
//            .filter { it.name == id }
//            .map { FileView(it) }
//            .first()

//    val found: FtpFile? = view.homeDirectory.listFiles().firstOrNull() { it.name == id }
//    if (found == null) {
//      return null
//    }
//    return FileView(found!!)
    return view.homeDirectory.listFiles()
            .map { FileView.create(it) }
            .firstOrNull { it.name == id } ?: return null
  }
//  fun list
}

