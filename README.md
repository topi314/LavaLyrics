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
    sources: # The order in which the lyrics sources will be queried
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

| Name       | Type                                 | Description                                                        |
|------------|--------------------------------------|--------------------------------------------------------------------|
| sourceName | string                               | The name of the source where the lyrics were fetched from          |
| provider   | string                               | The name of the provider the lyrics was fetched from on the source |
| text       | ?string                              | The lyrics text                                                    |
| lines      | Array of [Lyrics Line](#lyrics-line) | The lyrics lines                                                   |
| plugin     | object                               | Additional plugin specific data                                    |

##### Lyrics Line

| Name      | Type   | Description                               |
|-----------|--------|-------------------------------------------|
| timestamp | int    | The timestamp of the line in milliseconds |
| duration  | ?int   | The duration of the line in milliseconds  |
| line      | string | The lyrics line                           |
| plugin    | object | Additional plugin specific data           |

#### Events

LavaLyrics supports live lyrics events. You can subscribe to these events to get live lines for the lyrics of the current playing track.

For the base fields of the events see the [Lavalink Docs](https://lavalink.dev/api/websocket.html#event-op)

##### Event Types

| Name                  | Description                                                               |
|-----------------------|---------------------------------------------------------------------------|
| `LyricsFoundEvent`    | Sent when lyrics are found for a track and about to be sent to the client |
| `LyricsNotFoundEvent` | Sent when no lyrics were found for a track                                |
| `LyricsLineEvent`     | Sent when a new lyrics line is available for a track                      |

##### LyricsFoundEvent

This event is sent when lyrics are found for a track and about to be sent to the client.

| Name   | Type                            | Description                                                |
|--------|---------------------------------|------------------------------------------------------------|
| lyrics | [Lyrics Object](#lyrics-object) | The lyrics object containing the lyrics lines and metadata |


##### LyricsNotFoundEvent

This event is sent when no lyrics were found for a track.

This event does not contain any additional fields.

##### LyricsLineEvent

This event is sent when a new lyrics line is reached for a track.

| Name      | Type                        | Description                                             |
|-----------|-----------------------------|---------------------------------------------------------|
| lineIndex | int                         | The index of the line in the lyrics lines array         |
| line      | [Lyrics Line](#lyrics-line) | The lyrics line object containing the line and metadata |
| skipped   | boolean                     | Whether the line was skipped due to seeking             |

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

### Subscribe to live lyrics

You can subscribe to live lyrics lines for a given player.

```http
POST /v4/sessions/{sessionId}/players/{guildId}/lyrics/subscribe?skipTrackSource={skipTrackSource}
```

Query Params:

| Name            | Type    | Description                                                          |
|-----------------|---------|----------------------------------------------------------------------|
| skipTrackSource | boolean | Skip the current track source and fetch from highest priority source |

Response:

204 No Content:
- If the subscription was successful

#### Unsubscribe from live lyrics

You can unsubscribe from live lyrics lines for a given player.

```http
DELETE /v4/sessions/{sessionId}/players/{guildId}/lyrics/subscribe
```

Response:
204 No Content:
- If the unsubscription was successful

---

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

Create new `LyricsManager`:
```java
var lyricsManager = new LyricsManager();

// register your lyrics sources
lyricsManager.registerLyricsManager(source);
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
