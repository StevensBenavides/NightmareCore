package com.nightmare;

public final class Constants {

    public enum onJoin {

        prefix("prefix"),
        scoreboard("scoreboard"),
        scoreboard_enable("scoreboard.enable"),
        scoreboard_name("scoreboard.name"),	
        join_and_leave("join_and_leave"),
        join_and_leave_enable("join_and_leave.enable"),
        join_and_leave_join("join_and_leave.join"),
        join_and_leave_leave("join_and_leave.leave"),
        message_join("message_join"),
        message_join_title("message_join.title"),
        message_join_lines("message_join.lines"),
        config("config"),
        config_player("config.player"),	
        config_player_display_health("config.player.display_health");

        private final String value;

        onJoin(String value) {
            this.value = value;
        }

        public final String getValue() {
            
            return value;

        }

    }

    public enum onLeave {
        
        prefix("prefix"),
        scoreboard("scoreboard"),
        scoreboard_enable("scoreboard.enable"),
        scoreboard_name("scoreboard.name"),
        join_and_leave("join_and_leave"),
        join_and_leave_enable("join_and_leave.enable"),
        join_and_leave_join("join_and_leave.join"),
        join_and_leave_leave("join_and_leave.leave");

        private final String value;

        onLeave(String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }

    }

    public enum onServerPing {
        
        motd("motd"),
        motd_enable("motd.enable"),
        motd_lines("motd.lines");

        private final String value;

        onServerPing(String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }

    }

    public enum Mobs {
        config_mobs_name_c("config.mobs.c_tier"),
        config_mobs_name_b("config.mobs.b_tier"),
        config_mobs_name_a("config.mobs.a_tier");

        private final String value;

        Mobs(String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }
    }

    public enum onSpawnEvent {
        
        config("config"),
        config_mobs("config.mobs"),
        config_mobs_name_c("config.mobs.c_tier"),
        config_mobs_name_b("config.mobs.b_tier"),
        config_mobs_name_a("config.mobs.a_tier");
    
        private final String value;

        onSpawnEvent(String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }
        
    }

    public enum onPlayerDeath {

        message_death("message_death"),
        message_death_enable("message_death.enable"),
        message_death_message("message_death.message");

        private final String value;

        onPlayerDeath(String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }

    }
    
}
