ALTER TABLE Answer
    ADD COLUMN editModerator_id BIGINT,
    ADD CONSTRAINT FK_ANSWER2_ON_USER FOREIGN KEY (editModerator_id) REFERENCES user_entity(id);



