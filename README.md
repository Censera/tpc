# tpc

Small teleportation utility for Paper 26.2.

## Features

- Teleport requests with clickable Accept and Decline actions.
- Directional requests: `/tpa` asks to go to a player, `/tph` asks a player to come to you.
- Clickable player selection through `/tpa` and `/tph`.
- Paginated player selection for larger servers.
- Three persistent home slots by default.
- Primary homes.
- Bed, spawn, and previous-location teleportation.
- Teleport delay, cooldown, movement cancellation, and damage cancellation.
- Safe destination checks.
- Java and Geyser/Floodgate players use the same chat interface.
- No client-side mod or resource pack required.

## Commands

```text
/tpc [ask|here|accept|decline|bed|home]
/tpr # reload
/tpa [player|cancel|page <number>]
/tph [player|cancel|page <number>]
/accept
/decline
/back
/bed
/home
/home list
/home set <name> [is-primary]
/home delete <name>
/home primary <name>
/spawn
```

`/tpa <player>` sends a request to teleport **to** that player. `/tph <player>` sends a request asking that player to teleport **to you**. The two are tracked separately so `/accept` teleports the right person regardless of which one was used.

`/tpc` opens the clickable teleport menu, and also works as a shortcut for the other commands: `/tpc ask`, `/tpc here`, `/tpc accept`, `/tpc decline`, `/tpc bed`, and `/tpc home` behave the same as their standalone counterparts.

Normal players can use `/tpc` without the reload permission. `/tpr` reloads the configuration and requires `tpc.reload`.

## Alternative commands

```yaml
alternative-commands:
  enable: false
  tpaccept: true
  tpdecline: true
  tpback: true
  tpbed: true
  tphome: true
```

Some servers already have conflicting commands for `accept`, `decline`, `back`, `bed`, or `home`. When `alternative-commands.enable` is `true`, the corresponding flag below it turns on an equivalent shortcut command:

```text
/tpaccept -> /accept
/tpdecline -> /decline
/tpback -> /back
/tpbed -> /bed
/tphome -> /home
```

These are not a second implementation; they call the same handlers as the primary commands. When disabled, they respond that the command is disabled instead of doing anything.

## Standalone commands

```yaml
standalone-commands: true
```

`/tpa`, `/tph`, `/accept`, `/decline`, `/bed`, and `/home` normally work as top-level commands. Set `standalone-commands` to `false` to turn them off; players then have to go through `/tpc`, which covers the same ground:

```text
/tpc [<ask|here> [player|cancel|page <number>]|accept|decline|bed|home[list|set <name> [is-primary]|<delete|primary> <name>]]
```

`/tpr`, `/back`, and `/spawn` are not affected — `/back` and `/spawn` have no `/tpc` equivalent, and `/tpr` is an admin command, not one of these player-facing shortcuts.

## Build

Requires Java 25 and Maven.

```text
mvn verify
```

The Paper API is provided by the server and is not bundled into the plugin jar.

## Runtime behavior

Teleport requests expire according to `request-expiration` and are cancelled when either player disconnects. Teleports use the configured delay and cooldown. Damage cancellation is unchanged; movement cancellation only triggers once the player crosses into a different block on the X/Z plane, so falling, jumping in place, or standing on a slope will not cancel a pending teleport.

Homes are stored in `plugins/tpc/homes.yml` and are saved immediately after home mutations and again during plugin shutdown.

Invalid required configuration stops plugin startup with the exact configuration path that failed.
