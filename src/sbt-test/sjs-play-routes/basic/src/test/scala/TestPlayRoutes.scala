import utest._

import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Protocol.HTTP
import fr.hmil.roshttp.Protocol.HTTPS
import fr.hmil.roshttp.response.SimpleHttpResponse
import fr.hmil.roshttp.exceptions.HttpException

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import routes.xyz._0x7e.Blog

object TestPlayRoutes extends TestSuite {

  implicit val client =
    HttpRequest()
      .withProtocol(HTTPS)
      .withHost("jsonplaceholder.typicode.com")
      .withPort(443)

  private def checkHttpResponse(response: SimpleHttpResponse): Boolean =
    if(response.statusCode == 200)
      true
    else
      throw new IllegalStateException(s"Wrong status code: ${response.statusCode}")

  val tests = this {

    'root {
      Blog.index().map(checkHttpResponse)
    }

    'posts {
      Blog
        .posts().map(checkHttpResponse)
    }

    'post123 {
      Blog
        .posts().map(checkHttpResponse)
    }

  }

  tests.runAsync().map { results =>
    assert(results.toSeq(0).value.isSuccess) // GET /
    assert(results.toSeq(1).value.isSuccess) // GET /posts
    assert(results.toSeq(2).value.isSuccess) // GET /post/123
  }

}