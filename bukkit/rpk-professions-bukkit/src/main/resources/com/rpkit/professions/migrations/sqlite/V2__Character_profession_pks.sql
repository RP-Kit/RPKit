create table `rpkit_character_profession_2`(
   `character_id` int          NOT NULL,
   `profession`   varchar(256) NOT NULL
);

insert into `rpkit_character_profession_2`(`character_id`, `profession`)
    select `character_id`, `profession` from `rpkit_character_profession`;

drop table `rpkit_character_profession`;
alter table `rpkit_character_profession_2` rename to `rpkit_character_profession`;

create table `rpkit_character_profession_experience_2`(
    `character_id` int          NOT NULL,
    `profession`   varchar(256) NOT NULL,
    `experience`   int          NOT NULL,
    primary key(`character_id`, `profession`)
);

insert into `rpkit_character_profession_experience_2`(`character_id`, `profession`, `experience`)
    select `character_id`, `profession`, `experience` from `rpkit_character_profession_experience`;

drop table `rpkit_character_profession_experience`;
alter table `rpkit_character_profession_experience_2` rename to `rpkit_character_profession_experience`;