package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FtpHandler
import de.mirb.project.miftp.control.FtpProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.GetMapping


@Controller
class FileView {

  @Autowired
  lateinit var handler: FtpHandler
  @Autowired
  lateinit var ftpProvider: FtpProvider

  @GetMapping("/")
  fun index(model: Model): String {
    val user = ftpProvider.getUsername()
    val files = handler.listFiles(user)
    model.addAttribute("name", "sample")
    model.addAttribute("files", files)

    return "fileView"
  }
}