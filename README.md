<p align="center">
  <span>English</span> |
  <a href="https://github.com/lsfusion/platform/tree/master/README_ru.md#lsfusion-">Russian</a>
</p>

# lsFusion <a href="http://lsfusion.org" target="_blank"><img src="https://lsfusion.org/themes/lsfusion/assets/images/i-logo-lsfusion.svg" align="right"/></a>  

lsFusion is a free open-source information systems development platform based on the fifth-generation programming language of the same name.

Incremental computing, function-level, reactive, and event-based programming, the ability to write and iterate over all function values, multiple inheritance and polymorphism, no encapsulation, and lots of other fundamentally new ideas greatly improve the development speed, quality, and performance of created systems compared to traditional approaches.

## Main features
### **Single language for data**

The platform is free from [the object-relational semantic gap](https://en.wikipedia.org/wiki/Object-relational_impedance_mismatch), so developers don't need to constantly choose between "rapid" SQL queries and a "convenient" imperative language. Both of these approaches are almost fully abstracted and united. Each developer always works with data using a single paradigm, while the platform takes care of how and where to do all the remaining work.

### **No ORM, Yes SQL**

When a certain action requires data processing for many objects at once, the platform tries, whenever possible, to perform this processing on the database server through a single request (i.e. for all objects at once). In this case, all of the queries generated are optimized as much as possible depending on the features of the database server.

### **Absolute reactivity**

All computed data is automatically updated once the data it uses is changed. This rule applies always and everywhere, whether you're displaying interactive forms or simply accessing data inside the action being executed.

### **Dynamic physical model**

The platform allows you to materialize any existing indicators in the system at any time, add new tables or delete existing ones, or change the location of materialized indicators in these tables.

### **Constraints on any data**

The platform allows you to create constraints for the values of any data (even computed data), and all such constraints (as with events) are global, so inexperienced users or developers cannot bypass them with an invalid transaction.

### **Efficient client-server communication**

Client-server communication at the physical level minimizes synchronous round-trip calls (i.e. each user action leads to a single - usually asynchronous - request/response pair). The desktop client also archives and encrypts all transferred data (when necessary). During client-server communication (via TCP/IP for the desktop client or HTTP for the Web client), the platform provides guarantee of delivery - it resends any lost requests and ensures that they are processed in the correct order. All these features help the platform run efficiently even on low-bandwidth, unstable, or high-latency communication channels.

### **Everything as code**

All elements of the system, from events to form design, are written in the lsFusion language and stored in ordinary text files (without any shared repositories with an unknown structure). This allows you to use popular version control systems (Git, Subversion) and project-building tools (Maven in IDEs) when working with projects. In addition, this approach simplifies the support and deployment of the system you develop - you can use an ordinary text editor to view and quickly modify the logic when necessary and also easily identify any element in the system by file name and line number in this file.


### **Three-tier architecture**

The platform executes the imperative part of the system logic (i.e. everything related to data changes) on application servers and the declarative part (i.e. everything related to data calculations) on database servers. This separation simplifies the scaling of the system you develop and strengthens its fault tolerance due to the different nature of workload on these servers (e.g. using swap on an application server is much more dangerous than on a database server).

### **lsFusion programming language**

- Polymorphism and aggregations

    Supports inheritance (including multiple inheritance) and polymorphism (including multiple polymorphism). And, if inheritance isn't enough for you for whatever reason, the platform also provides an aggregation mechanism that, together with inheritance, allows you to implement almost any polymorphic logic.
    
- Modules and extensions

    The extension technique allows developers to extend the functionality of one module in another (e.g. they can modify forms or classes created in another module). This mechanism makes the solutions you create much more modular.

- Metaprogramming

    If you want to create your own high-level operator, or maybe you just don't know how to generalize the logic, but want to reuse it, lsFusion provides full support for automatic code generation, from both the server side and IDE.
   
- Namespaces
- Java & SQL integration
- Internationalization

### **Easy-to-use IDE**

Intellij IDEA-based IDE with everything developers could ever need: search for uses, code/error highlighting, smart auto-completion, quick jump to declarations, class structure, renaming of elements, usage tree, data change breakpoints, debuggers, and more.

### **Advanced tools for administrators**

The platform provides a complete set of administration tools for systems that are already running: interpreter (executes lsFusion code), process monitor (receives detailed information about processes that are running, e.g. call start time, stack, user, etc.), scheduler (executes periodic or scheduled actions), profiler (measures the performance of all actions executed for all/given users, e.g. builds a graph of calls, sharing time between the application server and the database server, etc.), messenger (for internal notifications/communication with users in the system), and numerous logs (connections, errors, etc.).

---

### **CI in [ACID](https://en.wikipedia.org/wiki/ACID) right out of the box**

The platform can properly roll back the application server to the state it was in before any data transaction had begun. This allows you to use the native integrity tools of modern SQL servers, which not only significantly reduces labor costs and human error when developing a system for a highly concurrent environment, but also makes such a system much more scalable (provided that versioning databases are used).

### **Advanced interactive UIs**

When interacting with a user, the platform can display any data not only as individual objects and lists, but also as trees, both flat ("nested") and recursive (e.g. classifiers). In addition, the platform allows you to group various form blocks into folders (tabs) for structuring large quantities of data. When the data is not visible to the user, it is not read/calculated (e.g. all lists are dynamic by default, i.e. only a limited number of objects are read initially, while the rest of them are read as the current object in the table is changed).

### **Clustering**

Since the application servers don't have any shared data, you can easily add as many of them as you need. In addition, the platform can transfer user data between database servers asynchronously, which, in turn, allows you to dynamically distribute the workload among them (e.g. when you add data to the database and need to use ACID, you can switch this processing to the master server or [in other cases] to the least busy slave servers).


### **Easy-to-use language**

The platform allows you to group element names into namespaces and consider names in the same namespace as having higher priority when searching for elements. In most cases, you only need to enter the "short" name of the element you're searching for and eventually write more concise and readable code. In addition, you can specify a class for each parameter in the code, and this class will also be taken into account during a search (i.e. explicit types are supported). This also makes your code shorter and simpler and ensures early error detection, smart auto-completion, and lots of other useful features.

### **Seamless linking of Java libraries / SQL functions**

The application server runs on a Java virtual machine (JVM) and can thus be integrated with countless Java libraries. To do this, just create your own action in Java, register it in the platform, and then use it just like any action built into the platform or written on the platform's built-in language. Accordingly, you can seamlessly connect custom SQL functions of the database server you're using.

### **Single paradigm for UI**

The platform doesn't split the user interface into interactive forms and reports. Any form can contain both primary and calculated data and can be displayed in a print (like a classic report) or interactive (editable) view. Accordingly, users can not only see the data they need to make a decision, but also immediately enter the decision they've made through the same UI.

### **WYSIWYG**

In almost all cases, the user gets lots of features right out of the box, including quick editing of any visible data and any visible objects (bulk editing), copy and paste (e.g. to/from Excel), merging/canceling/saving changes, and other WYSIWYG features.

### **Transparent integration**

The platform supports access to the system through the most common general-purpose application protocol (HTTP), while the access interface can execute not only certain actions (with automatic parsing of arguments), but also lsFusion code (as when accessing SQL servers). In addition, the platform supports access to any external systems at the language level via HTTP, as well as SQL servers, other lsFusion systems, etc. And, since the server itself is implemented as a Spring bean, you can manage its life cycle using a standard Spring IoC container. All these features allow you to use lsFusion not only as a full-fledged development platform, but also as a database or even a Java library.

### **Declarative handling of external formats**

Any form can be shown to the user interactively or printed out or exported in most popular data formats (JSON, XML, CSV, XLS, DBF, etc.). This operation is symmetrical - you can also import data from a file to a form in any of these formats. Both processes are absolutely declarative and use the same concepts as the UI, which greatly simplifies interaction with external systems.

### **Metaprogramming**

Do you want to create your own high-level operator, or maybe you just don't know how to generalize the logic, but want to reuse it? The platform provides full support for automatic code generation, from both the server side and IDE.

### **Open database structure**

The mapping of logic to the database is absolutely transparent, and developers can manage this process on their own. Forget about auto generated names and surrogate tables.

### **Cross-platform client**

The platform supports both desktop and Web clients with any OS for which JVM implementations are available. And since relative (rather than absolute) positioning is used for the components, they are rendered in the Web client natively using CSS (without JavaScript), which significantly increases UI responsiveness in the Web client. In addition, neither the desktop nor the Web client contain any application logic, which greatly simplifies their deployment (i.e. you can use the same client for any logic).

### **Cross-platform server**

Supports any OS for which JVM is implemented, as well as the most popular relational databases.

### **Data change events**

The platform allows you to create event handlers for any changes in any data (even computed data). This mechanism can significantly increase the modularity of the solutions created.

### **Asynchrony**

The platform tries, whenever possible, to perform all operations asynchronously (i.e. return control to the user before the operation completes), including most input operations and handling of local events.

### **Polymorphism and aggregations**

The platform supports inheritance (including multiple inheritance) and polymorphism (including multiple polymorphism). And, if inheritance isn't enough for you for whatever reason, the platform also provides an aggregation mechanism that, together with inheritance, allows you to implement almost any polymorphic logic.

### **Internationalization**

Within any string literal (e.g. an element title or string constant) the platform allows the use of IDs (in brackets) to which you can then assign different texts for different languages. Accordingly, when accessing this literal, the platform automatically localizes it depending on the language settings of the target user.


### **Server-to-client access**

The platform doesn't split business logic into server and client sides, which allows developers to request modal interaction with the user at any time. When this happens, the platform automatically pauses the current action, sends a request to the user, and resumes the action as soon as a response is received.

### **Retroactive changes**

The platform allows users to change any previously-entered data (e.g. to fix input errors) and only updates the data that is actually needed without thousands of related reversal operations, long reposting, or global database locks. In addition, the platform allows the simultaneous editing of the same data (e.g. the same document), which is useful for teamwork and rapidly inputting large amounts of data.


### **Security policy**

A flexible security policy allows you to define data access restrictions for both individual forms and specific actions/indicators. Then users won't be able to see this data or even know that it exists.

### **Open source code**

The open source code of the platform and IDE allows developers to study the internal mechanisms of the platform independently, create pull requests or custom builds, and license their own solutions for use in case of strict security requirements.

### **Deep user customization**

Users can customize any form, e.g. add custom selections and sorting rules or add/remove/reorder columns, and also perform basic analysis such as grouping visible data or calculating sums/maximums and other aggregated indicators. In addition to the UI settings, users can also add logging, fill-in control, or notifications about any data changes (including calculated data).

### **Free license**

The platform is licensed under LGPL v3, which allows you to freely use, distribute, and modify the platform as you wish.

## Installation
### Windows 
- **Development**

    Single-click .exe installers of **lsFusion 3.1** (OpenJDK 1.8.212, PostgreSQL 10.8, Tomcat 9.0.21, IntelliJ IDEA Community Edition 2019.1.3)  
    - [x32](http://download.lsfusion.org/exe/lsfusion-dev-3.1.exe)  
    - [x64](http://download.lsfusion.org/exe/lsfusion-dev-3.1-x64.exe)

    In addition to **lsFusion**, these installers also install **OpenJDK**, **PostgreSQL**, **Tomcat**, and **IntelliJ IDEA Community Edition** with the built-in **lsFusion plugin**. If any of these programs are already on your computer, you can exclude them during the installation process.

    After the installation completes successfully, the corresponding shortcuts for launching IDE and the client will automatically be created on the desktop. A description of working with IDE after it opens is located [here](https://documentation.lsfusion.org/display/LSFUS/IDE).

- **Production**

    Single-click .exe installers of **lsFusion 3.1 Server & Client** (OpenJDK 1.8.212, PostgreSQL 10.8, Tomcat 9.0.21)  
    - [x32](http://download.lsfusion.org/exe/lsfusion-3.1.exe)  
    - [x64](http://download.lsfusion.org/exe/lsfusion-3.1-x64.exe)

    In addition to **lsFusion**, these installers also install **OpenJDK**, **PostgreSQL**, and **Tomcat**. **Tomcat** is embedded into the **lsFusion Client** installation, and **OpenJDK** and **PostgreSQL** are installed separately (in particular, in separate folders).

### Linux
- **Development**

    1. [Install](https://www.jetbrains.com/help/idea/installation-guide.html) the **Intellij IDEA Community Edition**.
    1. Install the [lsFusion language support](https://plugins.jetbrains.com/plugin/7601-lsfusion) Intellij IDEA plugin. In the IDEA settings `File > Settings`, select `Plugins > Browse repositories`, find the **lsFusion** plugin, click **Install**, and restart IDEA. 
    1. Create a new lsFusion project in IDEA:
    
        1. Select **Create New Project**, or when IDEA is already opened, select `File > New > Project` from the menu. 
        1. Select project type **lsFusion**. Make sure that the JDK is set. 
        1. Click the **Download** button opposed to the lsFusion library: IDEA automatically downloads the JAR file of the latest (non-beta) version of the lsFusion server from the central server and installs this file as a dependency of this project (or rather, as its only module: `File > Project Structure > Modules > project name > Dependencies tab`). Also, if necessary, you can download another version of the server (different from the latest) or select a previously downloaded server JAR file on the local disk. 
    
    After the server starts, in the start log one of the last lines will be a line with a link to the JNLP file, which when run will automatically install the client using Java Web Start technology. 
   
- **Production**

    **lsFusion 3 Server & Client** (OpenJDK 1.8, PostgreSQL 11, Tomcat 9.0.21).

    - RHEL 7 / CentOS 7 / Fedora 29

          source <(curl -s https://download.lsfusion.org/yum/install-lsfusion3)

    - Ubuntu 18 / Debian 9

          source <(curl -s https://download.lsfusion.org/apt/install-lsfusion3)

For more detailed information about installation process please refer to [install section](https://documentation.lsfusion.org/display/LSFUS/Install) of documentation.
 
## Demo / try online?

## Code samples?

## Solutions

## Links
- [Homepage](https://lsfusion.org)
- [Documentation](https://documentation.lsfusion.org/)
- [Blog (ru)](https://habr.com/ru/company/lsfusion/blog/)
- [Q&A (ru)](https://ru.stackoverflow.com/questions/tagged/lsfusion)
- [Repository](https://github.com/lsfusion/platform)
- [Downloads](https://download.lsfusion.org/)

## Feedback
- [Issue tracker](https://github.com/lsfusion/platform/issues) 
- [Slack community](https://slack.lsfusion.org)

## License
The platform is licensed under [LGPL v3](http://www.gnu.org/licenses/lgpl-3.0.en.html), which allows you to freely use, distribute, and modify the platform as you wish.
