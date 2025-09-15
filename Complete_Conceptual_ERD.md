# Complete Conceptual ERD for EV Service Center Management System

## Entities:
1. **Customer**  
   - CustomerID (PK)  
   - Name  
   - ContactNumber  
   - Email  
   - Address  

2. **Vehicle**  
   - VehicleID (PK)  
   - CustomerID (FK)  
   - Make  
   - Model  
   - Year  
   - VIN  

3. **Service**  
   - ServiceID (PK)  
   - ServiceType  
   - Description  
   - Price  

4. **Appointment**  
   - AppointmentID (PK)  
   - CustomerID (FK)  
   - VehicleID (FK)  
   - ServiceID (FK)  
   - AppointmentDate  
   - Status  

5. **Technician**  
   - TechnicianID (PK)  
   - Name  
   - Specialization  
   - ContactNumber  

6. **Invoice**  
   - InvoiceID (PK)  
   - AppointmentID (FK)  
   - TotalAmount  
   - InvoiceDate  

## Relationships:
- A **Customer** can own multiple **Vehicles** (1 to Many)
- A **Vehicle** can have multiple **Appointments** (1 to Many)
- An **Appointment** is for one **Service** (Many to 1)
- A **Service** can be performed in multiple **Appointments** (1 to Many)
- An **Appointment** is handled by one **Technician** (Many to 1)
- An **Appointment** can have one **Invoice** (1 to 1)
- An **Invoice** belongs to one **Appointment** (1 to 1)

## ERD Diagram:

```
[Customer] 1---* [Vehicle]
[Customer] 1---* [Appointment]
[Vehicle] 1---* [Appointment]
[Service] 1---* [Appointment]
[Technician] 1---* [Appointment]
[Appointment] 1---1 [Invoice]
```