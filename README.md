[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.topi.wtf%2Freleases%2Fcom%2Fgithub%2Ftopi314%2Flavalyrics%2Flavalyrics%2Fmaven-metadata.xml)](https://maven.topi.wtf/#/releases/com/github/topi314/lavalyrics/lavalyrics)

# LavaLyrics

LavaLyrics is a lyrics module for [Lavaplayer](https://github.com/sedmelluq/lavaplayer) and [Lavalink](https://github.com/lavalink-devs/Lavalink).
It allows registering different lyrics sources and handles fetching from said sources.

You can use this module with Lavaplayer or as a plugin for Lavalink `v4.0.5` or higher. Other plugins can link into this module to provide additional lyrics sources.

<details>
<summary>Table of Contents</summary>

* [Supported Sources](#supported-sources)
* [Lavalink Usage](#lavalink-usage)
* [Lavaplayer Usage](#lavaplayer-usage)
    * [Using in Gradle:](#using-in-gradle)
    * [Using in Maven:](#using-in-maven)
    * [Usage](#usage)

</details>

## Supported Sources

* [LavaSrc](https://github.com/topi314/LavaSrc) (YouTube/YouTubeMusic, Deezer, Spotify, YandexMusic)
* [Java Timed Lyrics](https://github.com/DuncteBot/java-timed-lyrics/) (YouTube/YouTubeMusic, Genius)

*Add your own*

## Lavalink Usage

This plugin requires Lavalink `v4.0.5` or higher.

To install this plugin either download the latest release and place it into your `plugins` folder or add the following into your `application.yml`

> **Note**
> For a full `application.yml` example see [here](application.example.yml)

Replace x.y.z with the latest version number

```yaml
lavalink:
  plugins:
    - dependency: "com.github.topi314.lavalyrics:lavalyrics-plugin:x.y.z"
    #  snapshot: false # set to true if you want to use snapshot builds (see below)
```

Snapshot builds are available in https://maven.lavalink.dev/snapshots with the short commit hash as the version

### Configuration

The plugin can be configured in the `application.yml` file as follows:

(YES `plugins` IS AT ROOT IN THE YAML)

```yaml
plugins:
  lavalyrics:
    # sources is used to sort the different lyrics sources by priority (from highest to lowest)
    sources:
      - spotify
      - youtube
      - deezer
      - yandexMusic
```

### API

LavaLyrics provides a rest API to fetch lyrics for a given track.
Fields marked with `?` are optional and types marked with `?` are nullable.

#### Common Types

##### Lyrics Object

| Name       | Type                               | Description                                                        |
|------------|------------------------------------|--------------------------------------------------------------------|
| sourceName | string                             | The name of the source where the lyrics were fetched from          |
| provider   | string                             | The name of the provider the lyrics was fetched from on the source |
| text       | ?string                            | The lyrics text                                                    |
| lines      | Array of [LyricsLine](#lyricsline) | The lyrics lines                                                   |
| plugin     | object                             | Additional plugin specific data                                    |

##### LyricsLine

| Name      | Type   | Description                               |
|-----------|--------|-------------------------------------------|
| timestamp | int    | The timestamp of the line in milliseconds |
| duration  | int    | The duration of the line in milliseconds  |
| line      | string | The lyrics line                           |
| plugin    | object | Additional plugin specific data           |

#### Get current playing track lyrics

Gets the lyrics of the current playing track. By default, it will try to fetch the lyrics from where the track is sourced from.
E.g. if the track is from Deezer it will try to fetch the lyrics from Deezer. You can disable this behavior by setting `skipTrackSource` to `true`.

```http
GET /v4/sessions/{sessionId}/players/{guildId}/track/lyrics?skipTrackSource={skipTrackSource}
```

Query Params:

| Name            | Type    | Description                                                          |
|-----------------|---------|----------------------------------------------------------------------|
| skipTrackSource | boolean | Skip the current track source and fetch from highest priority source |

Response:

200 OK:
[Lyrics Object](#lyrics-object)

<details>
<summary>Example Payload</summary>

```yaml
{
  "sourceName": "spotify",
  "provider": "MusixMatch",
  "text": null,
  "lines": [
    {
      "timestamp": 16770,
      "duration": null,
      "line": "Took a walk to the water at night",
      "plugin": {}
    },
    ...
    {
      "timestamp": 214480,
      "duration": null,
      "line": "",
      "plugin": {}
    }
  ],
  "plugin": {}
}
```

</details>

404 Not Found:

- If there is no track playing in the guild

204 No Content:

- If no lyrics were found

#### Get lyrics for a track

Gets the lyrics for a given encoded track. By default, it will try to fetch the lyrics from where the track is sourced from.
E.g. if the track is from Deezer it will try to fetch the lyrics from Deezer. You can disable this behavior by setting `skipTrackSource` to `true`.

```http
GET /v4/lyrics?track={encodedTrack}&skipTrackSource={skipTrackSource}
```

Query Params:

| Name            | Type    | Description                                                          |
|-----------------|---------|----------------------------------------------------------------------|
| track           | string  | The encoded track to fetch lyrics for                                |
| skipTrackSource | boolean | Skip the current track source and fetch from highest priority source |

Response:

200 OK:
[Lyrics Object](#lyrics-object)

<details>
<summary>Example Payload</summary>

```yaml
{
  "sourceName": "spotify",
  "provider": "MusixMatch",
  "text": null,
  "lines": [
    {
      "timestamp": 16770,
      "duration": null,
      "line": "Took a walk to the water at night",
      "plugin": {}
    },
    ...
    {
      "timestamp": 214480,
      "duration": null,
      "line": "",
      "plugin": {}
    }
  ],
  "plugin": {}
}
```

</details>

204 No Content:

- If no lyrics were found

## Lavaplayer Usage
Replace x.y.z with the latest version number

Snapshot builds are available in https://maven.topi.wtf/snapshots with the short commit hash as the version

### Using in Gradle:

<details>
<summary>Gradle</summary>

```gradle
repositories {
  maven {
    url "https://maven.topi.wtf/releases"
  }
}

dependencies {
  implementation "com.github.topi314.lavalyrics:lavalyrics:x.y.z"
}
```
</details>

### Using in Maven:

<details>
<summary>Maven</summary>

```xml
<repositories>
  <repository>
    <url>https://maven.topi.wtf/releases</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.topi314.lavalyrics</groupId>
    <artifactId>lavalyrics</artifactId>
    <version>x.y.z</version>
  </dependency>
</dependencies>
```
</details>

### Usage

Register `Lyrics Manager`:
```java
LyricsManager lyricsManager = new LyricsManager();
```
Register soruces:
```java

var spotify = new SpotifySourceManager(clientId, clientSecret, spDc, countryCode, () -> audioPlayerManager, DefaultMirroringAudioTrackResolver)
var youtube = new YoutubeSearchManager(() -> audioPlayerManager, region);

lyricsManager.registerLyricsManager(spotify);
lyricsManager.registerLyricsManager(youtube);
```

Basic and example usage:
```java
var audioLyrics = lyricsManager.loadLyrics(AudioTrack, skipTrackSource);
```
Then you can do what you want with this:
```java
List<AudioLyrics.Line> lines = audioLyrics.getLines();
if (lines != null) {
        StringBuilder sb = new StringBuilder();
        for (AudioLyrics.Line line : lines) {
          sb.append(line.getLine()).append("\n");
        }
}
System.out.println(sb); // Output: Line by line text
```
