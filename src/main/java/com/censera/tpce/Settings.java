package com.censera.tpc;

record Settings(int requestExpirationSeconds, int teleportDelaySeconds, int teleportCooldownSeconds,
                 int homeLimit, boolean requireSafeDestination, boolean cancelOnMovement,
                 boolean cancelOnDamage, boolean standaloneCommandsEnabled,
                 boolean alternativeCommandsEnabled, boolean altTpAccept,
                 boolean altTpDecline, boolean altTpBack, boolean altTpBed,
                 boolean altTpHome, boolean altTpSpawn) {
}
