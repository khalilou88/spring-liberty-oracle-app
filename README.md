# Spring Application Deployment on WebSphere Liberty with Oracle Database

This guide provides a complete setup for deploying a traditional Spring application on WebSphere Liberty using ojdbc8 driver and Java 8, focusing on the backend components without views.

## Project Structure

```
spring-liberty-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           ├── config/
│   │   │           │   └── AppConfig.java
│   │   │           │   └── WebConfig.java
│   │   │           │   └── AppInitializer.java
│   │   │           ├── controller/
│   │   │           │   └── EmployeeController.java
│   │   │           ├── dao/
│   │   │           │   └── EmployeeDAO.java
│   │   │           ├── model/
│   │   │           │   └── Employee.java
│   │   │           └── service/
│   │   │               └── EmployeeService.java
│   │   ├── liberty/
│   │   │   └── config/
│   │   │       └── server.xml
│   │   ├── resources/
│   │   │   └── log4j.properties
│   └── test/
│       └── java/
```

## Maven Configuration (pom.xml)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
   <modelVersion>4.0.0</modelVersion>

   <groupId>com.example</groupId>
   <artifactId>spring-liberty-app</artifactId>
   <version>1.0-SNAPSHOT</version>
   <packaging>war</packaging>

   <properties>
      <maven.compiler.source>1.8</maven.compiler.source>
      <maven.compiler.target>1.8</maven.compiler.target>
      <spring.version>5.3.20</spring.version>
      <ojdbc8.version>21.5.0.0</ojdbc8.version>
      <hibernate.version>5.6.10.Final</hibernate.version>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
   </properties>

   <dependencies>
      <!-- Spring Framework -->
      <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-context</artifactId>
         <version>${spring.version}</version>
      </dependency>
      <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-webmvc</artifactId>
         <version>${spring.version}</version>
      </dependency>
      <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-jdbc</artifactId>
         <version>${spring.version}</version>
      </dependency>
      <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-orm</artifactId>
         <version>${spring.version}</version>
      </dependency>

      <!-- Servlet API -->
      <dependency>
         <groupId>javax.servlet</groupId>
         <artifactId>javax.servlet-api</artifactId>
         <version>4.0.1</version>
         <scope>provided</scope>
      </dependency>

      <!-- For RESTful services -->
      <dependency>
         <groupId>com.fasterxml.jackson.core</groupId>
         <artifactId>jackson-databind</artifactId>
         <version>2.13.3</version>
      </dependency>

      <!-- Oracle JDBC Driver -->
      <dependency>
         <groupId>com.oracle.database.jdbc</groupId>
         <artifactId>ojdbc8</artifactId>
         <version>${ojdbc8.version}</version>
         <scope>provided</scope>
      </dependency>

      <!-- Hibernate for JPA -->
      <dependency>
         <groupId>org.hibernate</groupId>
         <artifactId>hibernate-core</artifactId>
         <version>${hibernate.version}</version>
      </dependency>

      <!-- Logging -->
      <dependency>
         <groupId>org.slf4j</groupId>
         <artifactId>slf4j-api</artifactId>
         <version>1.7.36</version>
      </dependency>
      <dependency>
         <groupId>org.slf4j</groupId>
         <artifactId>slf4j-log4j12</artifactId>
         <version>1.7.36</version>
      </dependency>
      <dependency>
         <groupId>log4j</groupId>
         <artifactId>log4j</artifactId>
         <version>1.2.17</version>
      </dependency>
   </dependencies>

   <build>
      <finalName>spring-liberty-app</finalName>
      <plugins>
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
               <source>1.8</source>
               <target>1.8</target>
            </configuration>
         </plugin>

         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.3.2</version>
            <configuration>
               <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
         </plugin>

         <plugin>
            <groupId>io.openliberty.tools</groupId>
            <artifactId>liberty-maven-plugin</artifactId>
            <version>3.7.1</version>
            <configuration>
               <serverName>springServer</serverName>
               <include>usr</include>
               <bootstrapProperties>
                  <default.http.port>9080</default.http.port>
                  <default.https.port>9443</default.https.port>
                  <app.context.root>/api</app.context.root>
               </bootstrapProperties>
               <!-- Copy Oracle JDBC driver to server/lib directory -->
               <copyDependencies>
                  <dependencyGroup>
                     <location>lib</location>
                     <stripVersion>true</stripVersion>
                     <dependency>
                        <groupId>com.oracle.database.jdbc</groupId>
                        <artifactId>ojdbc8</artifactId>
                        <version>${ojdbc8.version}</version>
                     </dependency>
                  </dependencyGroup>
               </copyDependencies>
            </configuration>
         </plugin>
      </plugins>
   </build>
</project>
```

## Liberty Server Configuration (server.xml)

Create a file at `src/main/liberty/config/server.xml`:

```xml
<server description="Spring Application Server">
    <featureManager>
        <feature>servlet-4.0</feature>
        <feature>jdbc-4.2</feature>
        <feature>jndi-1.0</feature>
        <feature>localConnector-1.0</feature>
        <feature>jpa-2.2</feature>
    </featureManager>

    <httpEndpoint id="defaultHttpEndpoint" host="*" httpPort="${default.http.port}" httpsPort="${default.https.port}" />

    <library id="OracleLib">
        <fileset dir="${server.config.dir}/lib" includes="ojdbc8.jar" />
    </library>

    <dataSource id="OracleDataSource" jndiName="jdbc/myOracleDS">
        <jdbcDriver libraryRef="OracleLib" />
        <properties.oracle URL="jdbc:oracle:thin:@//localhost:1521/ORCLPDB1"
                          user="scott" 
                          password="tiger" />
        <connectionManager maxPoolSize="50" minPoolSize="10" />
    </dataSource>

    <applicationManager autoExpand="true" />
    
    <application id="spring-liberty-app" location="spring-liberty-app.war" name="spring-liberty-app" type="war" context-root="${app.context.root}">
        <classloader commonLibraryRef="OracleLib" />
    </application>
</server>
```

## Java Classes

### Model Class (Employee.java)

Create `src/main/java/com/example/model/Employee.java`:

```java
package com.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", sequenceName = "employee_seq", allocationSize = 1)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "salary")
    private Double salary;
    
    @Column(name = "email")
    private String email;

    // Default constructor
    public Employee() {
    }
    
    // Constructor with fields
    public Employee(String name, String department, Double salary, String email) {
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", name=" + name + ", department=" + department + 
               ", salary=" + salary + ", email=" + email + "]";
    }
}
```

### DAO Layer (EmployeeDAO.java)

Create `src/main/java/com/example/dao/EmployeeDAO.java`:

```java
package com.example.dao;

import com.example.model.Employee;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class EmployeeDAO {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Employee> getAllEmployees() {
        TypedQuery<Employee> query = entityManager.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class);
        return query.getResultList();
    }
    
    public Employee findById(Long id) {
        return entityManager.find(Employee.class, id);
    }
    
    public void save(Employee employee) {
        if (employee.getId() == null) {
            entityManager.persist(employee);
        } else {
            entityManager.merge(employee);
        }
    }
    
    public void delete(Long id) {
        Employee employee = findById(id);
        if (employee != null) {
            entityManager.remove(employee);
        }
    }
    
    public List<Employee> findByDepartment(String department) {
        TypedQuery<Employee> query = entityManager.createQuery(
            "SELECT e FROM Employee e WHERE e.department = :dept", Employee.class);
        query.setParameter("dept", department);
        return query.getResultList();
    }
}
```

### Service Layer (EmployeeService.java)

Create `src/main/java/com/example/service/EmployeeService.java`:

```java
package com.example.service;

import com.example.dao.EmployeeDAO;
import com.example.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeDAO employeeDAO;
    
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }
    
    public Employee findById(Long id) {
        return employeeDAO.findById(id);
    }
    
    public void saveEmployee(Employee employee) {
        employeeDAO.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        employeeDAO.delete(id);
    }
    
    public List<Employee> findByDepartment(String department) {
        return employeeDAO.findByDepartment(department);
    }
}
```

### Controller (EmployeeController.java)

Create `src/main/java/com/example/controller/EmployeeController.java`:

```java
package com.example.controller;

import com.example.model.Employee;
import com.example.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        employeeService.saveEmployee(employee);
        return new ResponseEntity<>(employee, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        employee.setName(employeeDetails.getName());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        employee.setEmail(employeeDetails.getEmail());
        
        employeeService.saveEmployee(employee);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteEmployee(@PathVariable Long id) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        employeeService.deleteEmployee(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping("/department")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@RequestParam String name) {
        List<Employee> employees = employeeService.findByDepartment(name);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
}
```

## Spring Configuration Classes

### WebConfig.java

Create a file at `src/main/java/com/example/config/WebConfig.java`:

```java
package com.example.config;

        import org.springframework.context.annotation.ComponentScan;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.web.servlet.config.annotation.EnableWebMvc;

        @Configuration
        @EnableWebMvc
        @ComponentScan(basePackages = "com.example.controller")
        public class WebConfig {
        // Additional configuration can go here if needed
        }

```

### AppConfig.java

Create a file at `src/main/java/com/example/config/AppConfig.java`:

```java
package com.example.config;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@ComponentScan(basePackages = {"com.example.service", "com.example.dao"})
@EnableTransactionManagement
public class AppConfig {

   @Bean
   public JndiObjectFactoryBean dataSource() {
      JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
      jndiObjectFactoryBean.setJndiName("jdbc/myOracleDS");
      jndiObjectFactoryBean.setResourceRef(true);
      return jndiObjectFactoryBean;
   }

   @Bean
   public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
      LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
      em.setDataSource((DataSource) dataSource().getObject());
      em.setPackagesToScan("com.example.model");

      HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
      vendorAdapter.setShowSql(true);
      vendorAdapter.setDatabasePlatform("org.hibernate.dialect.Oracle12cDialect");
      em.setJpaVendorAdapter(vendorAdapter);

      Properties properties = new Properties();
      properties.setProperty("hibernate.format_sql", "true");
      properties.setProperty("hibernate.use_sql_comments", "true");
      properties.setProperty("hibernate.hbm2ddl.auto", "update");
      em.setJpaProperties(properties);

      return em;
   }

   @Bean
   public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
      JpaTransactionManager transactionManager = new JpaTransactionManager();
      transactionManager.setEntityManagerFactory(emf);
      return transactionManager;
   }
}
```

### AppInitializer.java

Create a file at `src/main/java/com/example/config/AppInitializer.java`:

```java
package com.example.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class AppInitializer implements WebApplicationInitializer {

   @Override
   public void onStartup(ServletContext servletContext) throws ServletException {

      // Root application context (equivalent to applicationContext.xml)
      AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
      rootContext.setConfigLocation("com.example.config"); // package with @Configuration classes

      servletContext.addListener(new ContextLoaderListener(rootContext));

      // Dispatcher servlet context (equivalent to dispatcher-servlet.xml)
      AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
      dispatcherContext.register(WebConfig.class); // your WebConfig class

      ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher",
              new DispatcherServlet(dispatcherContext));

      dispatcher.setLoadOnStartup(1);
      dispatcher.addMapping("/");
   }
}

```

## Logging Configuration

Create a file at `src/main/resources/log4j.properties`:

```properties
# Root logger option
log4j.rootLogger=INFO, stdout

# Direct log messages to stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Hibernate logging options
log4j.logger.org.hibernate=INFO
log4j.logger.org.hibernate.SQL=DEBUG
log4j.logger.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Spring logging
log4j.logger.org.springframework=INFO
```

## Building and Deployment Steps

1. Build the application:
   ```bash
   mvn clean package
   ```

2. Create and configure Liberty server:
   ```bash
   mvn liberty:create liberty:install-feature
   ```

3. Deploy the application:
   ```bash
   mvn liberty:deploy
   ```

4. Start the Liberty server:
   ```bash
   mvn liberty:start
   ```

5. Your REST API will be accessible at:
   ```
   http://localhost:9080/api/employees
   ```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/employees | Get all employees |
| GET | /api/employees/{id} | Get employee by ID |
| POST | /api/employees | Create a new employee |
| PUT | /api/employees/{id} | Update an employee |
| DELETE | /api/employees/{id} | Delete an employee |
| GET | /api/employees/department?name={dept} | Get employees by department |

## Testing with curl

Here are some curl commands to test your API:

1. Get all employees:
   ```bash
   curl -X GET http://localhost:9080/api/employees
   ```

2. Get a specific employee:
   ```bash
   curl -X GET http://localhost:9080/api/employees/1
   ```

3. Create a new employee:
   ```bash
   curl -X POST http://localhost:9080/api/employees \
     -H "Content-Type: application/json" \
     -d '{"name":"John Doe","department":"IT","salary":75000,"email":"john.doe@example.com"}'
   ```

4. Update an employee:
   ```bash
   curl -X PUT http://localhost:9080/api/employees/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"John Doe","department":"Engineering","salary":80000,"email":"john.doe@example.com"}'
   ```

5. Delete an employee:
   ```bash
   curl -X DELETE http://localhost:9080/api/employees/1
   ```

6. Get employees by department:
   ```bash
   curl -X GET http://localhost:9080/api/employees/department?name=IT
   ```

## Important Notes

1. Replace the database connection details in `server.xml` with your actual Oracle database credentials.

2. The Oracle JDBC driver (ojdbc8.jar) needs to be accessible to your Liberty server. Make sure it's properly located in the server's lib directory.

3. If you encounter class loading issues, verify that the `commonLibraryRef` attribute in the application tag in `server.xml` is correctly pointing to your Oracle library.

4. This application uses JPA/Hibernate for database operations. The `hibernate.hbm2ddl.auto=update` property will attempt to create/update tables in your database schema.

5. Liberty's JPA implementation might differ slightly from standalone Hibernate. If you encounter any JPA-related issues, consult the WebSphere Liberty documentation for specifics.

6. When deploying in a production environment, remember to adjust the logging levels and security settings appropriately.
