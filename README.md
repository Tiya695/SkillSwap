# SkillSwap

SkillSwap is a barter learning web platform where people exchange knowledge without money. One user teaches a skill they know well, while learning another skill from their peer.

The platform automatically pairs users through mutual matching. When User A teaches Java and wants Guitar, SkillSwap finds User B who teaches Guitar and wants Java.

## Key Features

* Mutual and Partial Matching: Pairs users with reciprocal skills or one way interests.
* Swap Requests: Users agree on skills, message, and duration in days.
* Interactive Workspace: Built in live chat and video call integration.
* Skill Assessments: Teachers create multiple choice tests for learners to verify progress.
* Automated Grading: System grades tests immediately with percentage score and pass status.
* Reviews and Ratings: Leave star ratings and feedback upon swap completion.
* Profile Management: Manage bio, contact details, date of birth, and avatar photos.
* Admin Controls: Manage shared skill categories and platform catalog.

## Technology Stack

* Backend: Java 21 or Java 24, Spring Boot 3, Spring Data JPA, Spring Security, JWT
* Frontend: React, Vite, Tailwind CSS, React Router, Axios
* Database: MySQL 8 on port 3306

## Prerequisites

* Java Development Kit 21 or higher
* Node.js and npm
* MySQL Server running on default port 3306

## How to Run the Project

1. Database Setup
Create database named skillswap or let Spring Boot generate tables automatically on startup.
Optional manual import:
database/schema.sql

2. Start the Application
Execute the unified project launcher:
run.bat

Or start components individually:
run_backend.bat
run_frontend.bat

Access the web interface at:
http://localhost:5173

## Default Accounts

Admin Account:
* Email: admin@skillswap.com
* Password: Admin@123
* Role: ADMIN

New users can register directly from the registration page with any Gmail address.
