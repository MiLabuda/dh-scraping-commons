package com.milabuda.dhscrapingcommons.imagearchive

import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.sync.Semaphore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import software.amazon.awssdk.services.s3.S3Client
import java.net.http.HttpClient

internal const val DEFAULT_NETWORK_THROTTLE_PERMITS = 30

@AutoConfiguration
@ConditionalOnClass(S3Client::class)
@EnableConfigurationProperties(S3ImageBucketProperties::class)
class ImageArchiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun imageDownloaderHttpClient(): HttpClient = defaultImageDownloaderHttpClient()

    @Bean
    @ConditionalOnMissingBean
    fun imageDownloader(
        httpClient: HttpClient,
        userAgentProvider: UserAgentProvider,
    ): ImageDownloaderPort = ImageDownloader(httpClient, userAgentProvider)

    @Bean
    @ConditionalOnMissingBean
    fun s3ImageKeyBuilder(
        s3ImageBucketProperties: S3ImageBucketProperties,
    ): S3ImageKeyBuilder = S3ImageKeyBuilder(s3ImageBucketProperties)

    @Bean
    @ConditionalOnMissingBean
    fun s3Mediator(
        s3Client: S3Client,
    ): S3MediatorPort = S3Mediator(s3Client)

    @Bean
    @ConditionalOnMissingBean
    fun networkThrottle(): Semaphore = Semaphore(DEFAULT_NETWORK_THROTTLE_PERMITS)

    @Bean
    @ConditionalOnMissingBean(ImageArchiver::class)
    fun s3ImageArchiver(
        s3ImageBucketProperties: S3ImageBucketProperties,
        s3ImageKeyBuilder: S3ImageKeyBuilder,
        imageDownloader: ImageDownloaderPort,
        s3Mediator: S3MediatorPort,
        networkThrottle: Semaphore,
        meterRegistry: MeterRegistry,
        observationRegistry: ObservationRegistry,
    ): ImageArchiver = S3ImageArchiver(
        s3ImageBucketProperties,
        s3ImageKeyBuilder,
        imageDownloader,
        s3Mediator,
        networkThrottle,
        meterRegistry,
        observationRegistry,
    )
}
