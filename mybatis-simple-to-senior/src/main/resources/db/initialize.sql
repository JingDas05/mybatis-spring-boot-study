-- 注意这个地方不是单引号，是反单引号
CREATE TABLE `country` (
    `id` INT NOT NULL AUTO_INCREMENT ,
    `countryname` VARCHAR(255) NULL ,
    `countrycode` VARCHAR(255) NULL ,
    PRIMARY KEY (`id`)
)

INSERT country(`countryname`, `countrycode`) VALUES ('中国','CN'),( '美国','US'),('俄罗斯','RU'),('英国','GB'),('法国','FR');