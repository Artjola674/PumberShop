# PlumberShop
PlumberShop is an e-commerce application that provides a platform for customers to browse and purchase products. It integrates advanced functionalities to enhance user experience, monitoring, and automation, such as intelligent scheduling, logging, and notifications.
# Features
- **User Authentication:** Login, registration, password reset, and logout functionalities.
- **Intelligent Shift Scheduling:** Utilizes OptaPlanner to create optimized schedules for staff shifts.
- **Aspects for Logging:** Efficient logging mechanism using aspects for better traceability.
- **Monitoring and Notifications:** Monitors API execution times and sends email notifications if any execution exceeds 5 seconds.
- **Excel File Generation:** Generates Excel files for scheduled shifts.
- **PDF Billing:** Creates PDF invoices for orders, streamlining the billing process.
- **OpenAPI Documentation:** Detailed API documentation using Swagger/OpenAPI.
- **Log File Archiving:** Implements a policy to archive log files for better management.
- **Scheduled Jobs:** Regularly cleans up expired tokens to maintain system integrity.
- **MongoAudit:** Tracks and audits database changes for enhanced security.
- **Profiles:** Dev, Test
- **Testing:** Integration testing, Unit testing
- **Annotations:** Custom annotation for validating passwords
- **Notifications:** Use of Apache Kafka for email notifications

# Technologies Used
- **Backend:** Java, Spring Boot
- **OptaPlanner:** For optimizing schedules and shifts
- **Logging:** Aspect-oriented programming (AOP) for logging
- **Database:**  MongoDB
- **File Generation:** Apache POI (for Excel), iText (for PDF)
- **Documentation:** Swagger/OpenAPI
- **Notification:** Apache Kafka
