USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'chatAppDatabase')
BEGIN
    ALTER DATABASE chatAppDatabase SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE chatAppDatabase;
END
GO

CREATE DATABASE chatAppDatabase;
GO

USE chatAppDatabase;
GO

CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    password NVARCHAR(50) NOT NULL,
    onlineStatus BIT,
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE Contacts (
    UserID INT NOT NULL,
    ContactUserID INT NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (ContactUserID) REFERENCES Users(UserID),
    CONSTRAINT PK_Contacts PRIMARY KEY (UserID, ContactUserID)
);
GO

CREATE TABLE Message (
    MessageID INT IDENTITY(1,1) PRIMARY KEY,
    message NVARCHAR(255) NOT NULL,
    date DATETIME DEFAULT GETDATE(),
    senderID INT NOT NULL,
    receiverID INT NOT NULL,
    FOREIGN KEY (senderID) REFERENCES Users(UserID),
    FOREIGN KEY (receiverID) REFERENCES Users(UserID)
);
GO

USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = 'root')
BEGIN
    CREATE LOGIN root WITH PASSWORD = '1234';
END
GO

USE chatAppDatabase;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'root')
BEGIN
    CREATE USER root FOR LOGIN root;
    EXEC sp_addrolemember 'db_owner', 'root';
END
GO
