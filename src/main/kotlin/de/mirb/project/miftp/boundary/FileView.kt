package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FileAccessHandler
import de.mirb.project.miftp.config.BeanProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class FileView {

  @Autowired
  lateinit var handler: FileAccessHandler
  @Autowired
  lateinit var beanProvider: BeanProvider

  @Value("\${server.path.prefix:}")
  private var pathPrefix: String? = null


  @GetMapping(value = [ "/", "" ])
  fun index(model: Model): String {
    val user = beanProvider.getUsername()
    val files = handler.listFiles(user).sortedByDescending { it.lastModified }
    model.addAttribute("name", "sample")
    model.addAttribute("files", files)
    model.addAttribute("urlPrefix", pathPrefix)

    return "fileView"
  }
}