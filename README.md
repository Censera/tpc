# tpc

Lightweight teleportation utility for Paper 26.2.

## Features

- Teleport requests with clickable Accept and Decline actions.
- Directional requests: `/tpa` asks to go to a player, `/tph` asks a player to come to you.
- Clickable player selection with pagination.
- Persistent homes with configurable home limits and primary homes.
- Bed, spawn, and previous-location teleportation.
- Configurable teleport delay and cooldown.
- Optional cancellation on movement or damage.
- Optional safe-destination checks.
- Java and Geyser/Floodgate players use the same chat interface.
- No client-side mod or resource pack required.

## Commands

### Main interface

```text
/tpc
/tpc ask [player|cancel|page <number>]
/tpc here [player|cancel|page <number>]
/tpc accept
/tpc decline
/tpc bed
/tpc home [list|set <name> [is-primary]|delete <name>|primary <name>]
/tpc spawn
```

Running `/tpc` without arguments opens the clickable teleport menu.

`/tpc ask` requests teleportation to another player. `/tpc here` asks another player to teleport to you. Both support direct player names, cancellation, and paginated player selection.

### Standalone commands

Standalone commands are enabled by default:

```yaml
standalone-commands: true
```

When enabled, these commands are available directly:

```text
/tpa [player|cancel|page <number>]
/tph [player|cancel|page <number>]
/accept
/decline
/back
/bed
/home [list|set <name> [is-primary]|delete <name>|primary <name>]
/spawn
```

Set `standalone-commands` to `false` to use `/tpc` as the player-facing interface instead. `/tpr` remains available for configuration reloads, and `/back` and `/spawn` are not controlled by this setting.

### Homes

```text
/home
/home list
/home set <name> [is-primary]
/home delete <name>
/home primary <name>
```

With no arguments, `/home` teleports to the primary home when one is set. Otherwise it opens the home list.

Homes are persistent and stored in `plugins/tpc/homes.yml`.

### Other destinations

```text
/back
/bed
/spawn
```

`/back` returns to the previous location recorded after a successful teleport. `/bed` uses the player's current respawn location, and `/spawn` uses the current world spawn.

### Reload

```text
/tpr
```

Reloads the configuration and requires `tpc.reload`.

## Teleport requests

`/tpa <player>` asks to teleport to that player.

`/tph <player>` asks that player to teleport to you.

Requests are tracked separately and can be accepted or declined with `/accept` and `/decline`. Requests expire according to `request-expiration` and are cancelled when either player disconnects.

## Alternative commands

Alternative command names can be enabled when a server already uses the normal command names.

```yaml
alternative-commands:
  enable: false
  tpaccept: true
  tpdecline: true
  tpback: true
  tpbed: true
  tphome: true
  tpspawn: true
```

The aliases are:

```text
/tpaccept -> /accept
/tpdecline -> /decline
/tpback -> /back
/tpbed -> /bed
/tphome -> /home
/tpspawn -> /spawn
```

The individual flags control each alias. When the alternative command group or an individual alias is disabled, that command is restricted from normal players.

## Configuration

Configuration is stored in:

```text
plugins/tpc/config.yml
```

Default configuration:

```yaml
request-expiration: 60
teleport-delay: 3
teleport-cooldown: 5

standalone-commands: true

alternative-commands:
  enable: false
  tpaccept: true
  tpdecline: true
  tpback: true
  tpbed: true
  tphome: true
  tpspawn: true

homes:
  limit: 3

safety:
  require-safe-destination: false
  cancel-on-movement: false
  cancel-on-damage: false
```

### Request expiration

`request-expiration` is the number of seconds before an unanswered teleport request expires.

### Teleport delay

`teleport-delay` is the number of seconds between starting a teleport and completing it. Set it to `0` for immediate teleports.

### Teleport cooldown

`teleport-cooldown` is the number of seconds before the player can start another teleport after a successful teleport. Set it to `0` to disable the cooldown.

### Home limit

`homes.limit` controls the maximum number of homes each player can store.

### Safe destinations

When `safety.require-safe-destination` is enabled, the destination must have passable space for the player's feet and head with solid ground underneath.

### Movement cancellation

When `safety.cancel-on-movement` is enabled, moving into another X/Z block during the teleport delay cancels the pending teleport. Movement within the same X/Z block does not cancel it.

### Damage cancellation

When `safety.cancel-on-damage` is enabled, taking damage during the teleport delay cancels the pending teleport.

Configuration can be reloaded with `/tpr` without restarting the server. Invalid required configuration prevents the plugin from accepting the configuration and reports the failed configuration path.

## Permissions

```text
tpc.command
tpc.reload
tpc.request
tpc.here
tpc.accept
tpc.decline
tpc.back
tpc.bed
tpc.home
tpc.spawn
```

Alternative commands use the corresponding command permission. Command permission defaults are adjusted according to the standalone and alternative command configuration so disabled command groups are restricted from normal players.

## Runtime behavior

Teleports use the configured delay and cooldown. Important state such as delays, cooldowns, cancellations, and failures is reported in chat without automatically opening the teleport menu.

Home data is saved after home mutations and again during plugin shutdown.

## Build

Requires Java 25 and Maven.

```text
mvn verify
```

The Paper API is provided by the server and is not bundled into the plugin jar.
