package com.milabuda.dhscrapingcommons.imagearchive

import com.milabuda.dhscrapingcommons.util.UtilAutoConfiguration
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import io.mockk.mockk
import kotlinx.coroutines.sync.Semaphore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import java.net.http.HttpClient

class ImageArchiveAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                UtilAutoConfiguration::class.java,
                ImageArchiveAutoConfiguration::class.java,
            )
        )
        .withUserConfiguration(MinimalInfraConfig::class.java)
        .withPropertyValues(
            "aws.s3.bucket.images.name=test-bucket",
            "aws.s3.bucket.images.project-prefix=test-prefix",
            "aws.s3.bucket.images.region=pl",
        )

    // -------------------------------------------------------------------------
    // Bean registration — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `all image-archive beans are registered when S3Client is present`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(ImageDownloaderPort::class.java)
            assertThat(ctx).hasSingleBean(S3ImageKeyBuilder::class.java)
            assertThat(ctx).hasSingleBean(S3MediatorPort::class.java)
            assertThat(ctx).hasSingleBean(Semaphore::class.java)
            assertThat(ctx).hasSingleBean(ImageArchiver::class.java)
        }
    }

    @Test
    fun `S3ImageBucketProperties are bound from application properties`() {
        contextRunner
            .withPropertyValues(
                "aws.s3.bucket.images.name=my-bucket",
                "aws.s3.bucket.images.project-prefix=my-project",
                "aws.s3.bucket.images.region=de",
            )
            .run { ctx ->
                val props = ctx.getBean(S3ImageBucketProperties::class.java)
                assertThat(props.name).isEqualTo("my-bucket")
                assertThat(props.projectPrefix).isEqualTo("my-project")
                assertThat(props.region).isEqualTo("de")
            }
    }

    // -------------------------------------------------------------------------
    // @ConditionalOnMissingBean — user-provided beans are NOT overridden
    // -------------------------------------------------------------------------

    @Test
    fun `user-provided ImageArchiver bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomImageArchiverConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(ImageArchiver::class.java)
                val archiver = ctx.getBean(ImageArchiver::class.java)
                assertThat(archiver).isSameAs(CustomImageArchiverConfig.INSTANCE)
            }
    }

    @Test
    fun `user-provided S3ImageKeyBuilder bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomKeyBuilderConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(S3ImageKeyBuilder::class.java)
                val builder = ctx.getBean(S3ImageKeyBuilder::class.java)
                assertThat(builder).isSameAs(CustomKeyBuilderConfig.INSTANCE)
            }
    }

    @Test
    fun `user-provided networkThrottle Semaphore bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomSemaphoreConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(Semaphore::class.java)
                val semaphore = ctx.getBean(Semaphore::class.java)
                assertThat(semaphore).isSameAs(CustomSemaphoreConfig.INSTANCE)
            }
    }

    @Test
    fun `user-provided HttpClient bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomHttpClientConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(HttpClient::class.java)
                val client = ctx.getBean(HttpClient::class.java)
                assertThat(client).isSameAs(CustomHttpClientConfig.INSTANCE)
            }
    }

    // -------------------------------------------------------------------------
    // Missing required property — context should fail to start
    // -------------------------------------------------------------------------

    @Test
    fun `context fails to start when required bucket name property is missing`() {
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    UtilAutoConfiguration::class.java,
                    ImageArchiveAutoConfiguration::class.java,
                )
            )
            .withUserConfiguration(MinimalInfraConfig::class.java)
            .withPropertyValues(
                "aws.s3.bucket.images.project-prefix=test-prefix",
                "aws.s3.bucket.images.region=pl",
                // name intentionally omitted
            )
            .run { ctx ->
                assertThat(ctx).hasFailed()
            }
    }

    // -------------------------------------------------------------------------
    // Shared test configurations
    // -------------------------------------------------------------------------

    @Configuration
    class MinimalInfraConfig {
        @Bean
        fun s3Client(): S3Client = mockk(relaxed = true)

        @Bean
        fun meterRegistry(): MeterRegistry = mockk(relaxed = true)

        @Bean
        fun observationRegistry(): ObservationRegistry = mockk(relaxed = true)
    }

    @Configuration
    class CustomImageArchiverConfig {
        companion object {
            val INSTANCE: ImageArchiver = mockk()
        }

        @Bean
        fun imageArchiver(): ImageArchiver = INSTANCE
    }

    @Configuration
    class CustomKeyBuilderConfig {
        companion object {
            val INSTANCE: S3ImageKeyBuilder = mockk()
        }

        @Bean
        fun s3ImageKeyBuilder(): S3ImageKeyBuilder = INSTANCE
    }

    @Configuration
    class CustomSemaphoreConfig {
        companion object {
            val INSTANCE: Semaphore = Semaphore(5)
        }

        @Bean
        fun networkThrottle(): Semaphore = INSTANCE
    }

    @Configuration
    class CustomHttpClientConfig {
        companion object {
            val INSTANCE: HttpClient = HttpClient.newHttpClient()
        }

        @Bean
        fun imageDownloaderHttpClient(): HttpClient = INSTANCE
    }
}
