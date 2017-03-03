var RPKSkill = Java.type("com.rpkit.skills.bukkit.skills.RPKSkill");
var FireballSkill = Java.extend(RPKSkill, {
    getName: function() {
        return "Fireball"
    },
    getManaCost: function() {
        return 2;
    },
    getCooldown: function() {
        return 10;
    },
    use: function(character) {
        var player = character.player;
        if (player == null) return;
        var bukkitPlayer = player.bukkitPlayer;
        if (bukkitPlayer == null) return;
        if (!bukkitPlayer.isOnline()) return;
        var bukkitOnlinePlayer = bukkitPlayer.player;
        var Fireball = Java.type("org.bukkit.entity.Fireball");
        bukkitOnlinePlayer.launchProjectile(Fireball.class);
    },
    canUse: function(character) {
        var RPKSkillTypeProvider = Java.type("com.rpkit.skills.bukkit.skills.RPKSkillTypeProvider");
        var skillTypeProvider = core.getServiceManager().getServiceProvider(RPKSkillTypeProvider.class);
        var skillType = skillTypeProvider.getSkillType("magic_offence");
        var RPKSkillPointProvider = Java.type("com.rpkit.skills.bukkit.skills.RPKSkillPointProvider");
        var skillPointProvider = core.getServiceManager().getServiceProvider(RPKSkillPointProvider.class);
        var magicOffenceSkillPoints = skillPointProvider.getSkillPoints(character, skillType);
        if (magicOffenceSkillPoints >= 5) {
            return true;
        } else {
            return false;
        }
    }
});
var skill = new FireballSkill();
