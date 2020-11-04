-- Project table
CREATE TABLE PROJECT (
		NAME VARCHAR(512) NOT NULL,
		REPOSITORY_URL VARCHAR(512),
		EXTERNAL_ID VARCHAR(512)
	);
	
CREATE UNIQUE INDEX PK_PROJECT ON PROJECT (NAME ASC);
ALTER TABLE PROJECT ADD CONSTRAINT PK_PROJECT UNIQUE (NAME);
ALTER TABLE PROJECT ADD CONSTRAINT CONSTRAINT_PK_PROJECT PRIMARY KEY (NAME);

-- We want to keep task URL and summary independent of Mylyn
ALTER TABLE TRACKEDTASK ADD COLUMN TASK_URL VARCHAR(512);
ALTER TABLE TRACKEDTASK ADD COLUMN TASK_SUMMARY VARCHAR(512);
ALTER TABLE TRACKEDTASK ADD COLUMN PROJECT VARCHAR(512);

-- Activities can be added directly to a projcet
ALTER TABLE ACTIVITY ADD COLUMN PROJECT VARCHAR(512);

