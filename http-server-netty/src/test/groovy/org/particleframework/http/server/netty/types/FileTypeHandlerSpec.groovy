package org.particleframework.http.server.netty.types

import okhttp3.Request
import org.particleframework.context.annotation.Requires
import org.particleframework.http.HttpStatus
import org.particleframework.http.annotation.Controller
import org.particleframework.http.annotation.Get
import org.particleframework.http.server.netty.AbstractParticleSpec
import org.particleframework.http.server.netty.types.files.FileTypeHandlerConfiguration
import org.particleframework.http.server.types.files.AttachedFile

import java.text.SimpleDateFormat
import static org.particleframework.http.HttpHeaders.*

class FileTypeHandlerSpec extends AbstractParticleSpec {

    private static File tempFile

    static {
        tempFile = File.createTempFile("fileTypeHandlerSpec", ".html")
        tempFile.write("<html><head></head><body>HTML Page</body></html>")
        tempFile
    }

    void "test returning a file from a controller"() {
        when:
        def request = new Request.Builder()
                .url("$server/test/html")
                .get()

        def response = client.newCall(
                request.build()
        ).execute()
        FileTypeHandlerConfiguration config = new FileTypeHandlerConfiguration()
        SimpleDateFormat dateFormat = new SimpleDateFormat(config.dateFormat)
        dateFormat.timeZone = config.dateTimeZone

        then:
        response.code() == HttpStatus.OK.code
        response.header(CONTENT_TYPE) == "text/html"
        Integer.parseInt(response.header(CONTENT_LENGTH)) > 0
        dateFormat.parse(response.header(DATE)) < dateFormat.parse(response.header(EXPIRES))
        response.header(CACHE_CONTROL) == "private, max-age=60"
        response.header(LAST_MODIFIED) == dateFormat.format(new Date(tempFile.lastModified()))
        response.body().string() == "<html><head></head><body>HTML Page</body></html>"
    }

    void "test 304 is returned if the correct header is sent"() {
        when:
        FileTypeHandlerConfiguration config = new FileTypeHandlerConfiguration()
        SimpleDateFormat dateFormat = new SimpleDateFormat(config.dateFormat)
        dateFormat.timeZone = config.dateTimeZone
        def request = new Request.Builder()
                .url("$server/test/html")
                .header(IF_MODIFIED_SINCE, dateFormat.format(new Date(tempFile.lastModified())))
                .get()

        def response = client.newCall(
                request.build()
        ).execute()

        then:
        response.code() == HttpStatus.NOT_MODIFIED.code
        response.header(DATE)
    }

    void "test what happens when a file isn't found"() {
        when:
        def request = new Request.Builder()
                .url("$server/test/notFound")
                .get()

        def response = client.newCall(
                request.build()
        ).execute()

        then:
        response.code() == HttpStatus.INTERNAL_SERVER_ERROR.code
        response.body().string() == '{"message":"Internal Server Error: Could not find file"}'
    }

    void "test when an attached file is returned"() {
        when:
        def request = new Request.Builder()
                .url("$server/test/download")
                .get()

        def response = client.newCall(
                request.build()
        ).execute()
        FileTypeHandlerConfiguration config = new FileTypeHandlerConfiguration()
        SimpleDateFormat dateFormat = new SimpleDateFormat(config.dateFormat)
        dateFormat.timeZone = config.dateTimeZone

        then:
        response.code() == HttpStatus.OK.code
        response.header(CONTENT_TYPE) == "text/html"
        response.header(CONTENT_DISPOSITION).startsWith("attachment; filename=\"fileTypeHandlerSpec")
        Integer.parseInt(response.header(CONTENT_LENGTH)) > 0
        dateFormat.parse(response.header(DATE)) < dateFormat.parse(response.header(EXPIRES))
        response.header(CACHE_CONTROL) == "private, max-age=60"
        response.header(LAST_MODIFIED) == dateFormat.format(new Date(tempFile.lastModified()))
        response.body().string() == "<html><head></head><body>HTML Page</body></html>"
    }

    void "test when an attached file is returned with a name"() {
        when:
        def request = new Request.Builder()
                .url("$server/test/differentName")
                .get()

        def response = client.newCall(
                request.build()
        ).execute()
        FileTypeHandlerConfiguration config = new FileTypeHandlerConfiguration()
        SimpleDateFormat dateFormat = new SimpleDateFormat(config.dateFormat)
        dateFormat.timeZone = config.dateTimeZone

        then: "the content type is still based on the file extension"
        response.code() == HttpStatus.OK.code
        response.header(CONTENT_TYPE) == "text/html"
        response.header(CONTENT_DISPOSITION) == "attachment; filename=\"abc.xyz\""
        Integer.parseInt(response.header(CONTENT_LENGTH)) > 0
        dateFormat.parse(response.header(DATE)) < dateFormat.parse(response.header(EXPIRES))
        response.header(CACHE_CONTROL) == "private, max-age=60"
        response.header(LAST_MODIFIED) == dateFormat.format(new Date(tempFile.lastModified()))
        response.body().string() == "<html><head></head><body>HTML Page</body></html>"
    }

    @Controller
    @Requires(property = 'spec.name', value = 'FileTypeHandlerSpec')
    static class TestController {

        @Get
        File html() {
            tempFile
        }

        @Get
        File notFound() {
            new File('/xyzabc')
        }

        @Get
        AttachedFile download() {
            new AttachedFile(tempFile)
        }

        @Get
        AttachedFile differentName() {
            new AttachedFile(tempFile, "abc.xyz")
        }
    }
}