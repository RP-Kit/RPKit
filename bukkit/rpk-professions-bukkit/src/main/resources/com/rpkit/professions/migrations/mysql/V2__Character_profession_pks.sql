alter table `rpkit_character_profession` drop primary key;
alter table `rpkit_character_profession_experience` add primary key(`character_id`, `profession`)