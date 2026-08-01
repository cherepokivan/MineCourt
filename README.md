# MineCourt

MineCourt is a lightweight court system plugin for Paper servers. Players can submit court cases against other players, while administrators can appoint a judge and close cases.

## Features

- Submit a court case with a player name and reason.
- Notify every online player when a case is submitted.
- Send the appointed judge an additional notification containing the reason.
- Notify the defendant that they must appear in court within 3–5 minutes.
- Browse all open cases, including plaintiff, defendant, reason, and submission date.
- Appoint a judge.
- Close and remove cases from the list.
- Persist the judge and all open cases across server restarts.

## Commands

`/court` and `/суд` are identical command aliases.

| Command | Description | Permission |
| --- | --- | --- |
| `/court create <player> <reason>` | Submit a court case against a player. | Everyone |
| `/court view` | View all open court cases. | Everyone |
| `/court close <number>` | Close and permanently remove a case. Use its number from `/court view`. | `minecourt.setjudge` / OP |
| `/court setjudge <player>` | Appoint a player as the court judge. | `minecourt.setjudge` / OP |

## Notifications

When a case is created:

- All online players receive: `Player <plaintiff> filed a court case against <defendant>.`
- The appointed judge receives the same message with the case reason.
- The defendant receives: `A court case has been filed against you! Appear within 3–5 minutes!`

When a case is closed, every online player receives a notification.

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `minecourt.setjudge` | Allows appointing a judge and closing cases. | OP |

## Language

Set the `Languale` value in `plugins/MineCourt/config.yml` to choose the plugin language:

```yml
Languale: RU
```

Available values are `RU` and `EN`. On first launch, MineCourt creates `RU.yml` and `EN.yml` in its plugin folder. Edit the selected language file to customise any player-facing message. Restart the server after changing the language or messages.

## Installation

1. Download `MineCourt.jar` from the Modrinth release or GitHub Actions artifact.
2. Place it in your Paper server's `plugins` folder.
3. Start or restart the server.
4. Appoint a judge with `/court setjudge <player>`.

## Requirements

- Java `25`

## Data storage

MineCourt stores the appointed judge and open court cases in:

```text
plugins/MineCourt/config.yml
```

Players specified in `create` or `setjudge` must be online or have joined the server at least once.

## Building from source

This repository includes a GitHub Actions workflow that builds the plugin on every push and pull request. The compiled JAR is uploaded as the `MineCourt` workflow artifact.
