package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FtpHandler
import de.mirb.project.miftp.control.FtpProvider
import io.netty.handler.codec.http.HttpMethod.GET
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class FileView {

  @Autowired
  lateinit var handler: FtpHandler
  @Autowired
  lateinit var ftpProvider: FtpProvider

  @Value("\${server.path.prefix:}")
  private var pathPrefix: String? = null


  @GetMapping(value = [ "/", "" ])
  fun index(model: Model): String {
    val user = ftpProvider.getUsername()
    val files = handler.listFiles(user).sortedByDescending { it.lastModified }
    model.addAttribute("name", "sample")
    model.addAttribute("files", files)
    model.addAttribute("urlPrefix", pathPrefix)

    return "fileView"
  }
}