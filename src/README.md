






<!-- PROJECT LOGO -->  
<br />  
<div align="center">  
<a href="https://github.com/github_username/repo_name">  
</a>  

<h3 align="center">JBackUp</h3>

<p align="center">  
A desktop app (and its corresponding server program) to efficiently back up files between devices. 
Supports multiple users with username/password authentication. Implemented in Java 20 with Swing, multithreading, and Sockets. 
<br />  
<br />  
<a href="https://github.com/github_username/repo_name">Download Compiled JARs</a>  
·  
<a href="https://github.com/github_username/repo_name/issues">Read the JavaDocs</a>  

</p>  
</div>  



<!-- TABLE OF CONTENTS -->  
<details>  
<summary>Table of Contents</summary>  
<ol>  
<li>  
<a href="#getting-started">Getting Started</a>  
<ul>  
<li><a href="#prerequisites">Prerequisites</a></li>  
<li><a href="#installation">Installation</a></li>  
</ul>  
</li>  
<li><a href="#usage">Usage</a></li>  
<li><a href="#license">License</a></li>  
<li><a href="#contact">Contact</a></li>  
</ol>  
</details>  







<!-- GETTING STARTED -->  
## Getting Started

To use the pre-compiled JAR file: ***CLICK HERE***
Otherwise:

### Prerequisites
You will need Java 20 on Windows, Mac, or Linux. Please add an exception to firewalls or file access control software to avoid functionality issues.
#### Windows (requires  `java` and `git` to be installed to `path`):
1. Open Command Prompt or PowerShell.
2. Run  `cd C:/Your/Directory/Here && git clone INSERT_URL_HERE`.
3. Run `javac ServerManager.java && javac ClientInstance.java`
4. Run `java ServerManager.java` to start the Server Management Utility. Make sure to start the server by pressing `Start Server`!
5. Run `java ClientInstance.java` to start the client-side interface.

#### Linux/Mac (requires  `java` and `git` to be installed to `path`):
1. Open Terminal.
2. Run  `cd /usr/Your/Directory/Here && git clone INSERT_URL_HERE`.
3. Run `javac ServerManager.java && javac ClientInstance.java`
4. Run `java ServerManager.java` to start the Server Management Utility. Make sure to start the server by pressing `Start Server`!
5. Run `java ClientInstance.java` to start the client-side interface.


<p align="right">(<a href="#readme-top">back to top</a>)</p>  



<!-- USAGE EXAMPLES -->  
## Usage
### Server Side -- General:
- Press "Start Server" to start the backup server on the port you previously entered.
- Press "Stop Server" to stop the backup server.
- Press "Restart Server" to restart the backup server.
- SCREENSHOT

### Server Side -- User Management:

- Press "Add User" to add a new user (note that the minimum password length is 6 characters). Usernames cannot be duplicated.
- Press "Delete User" to delete a user.
- Press "Change User Password" to change the user's password.
- SCREENSHOT
### Server Side -- Server Settings:
- Enter a port number in the "Port Number" field (note that ports must be $\geq$ 10000).
- Press "Choose Storage Directory" to choose the base file storage path for your server (note that you must have the proper permissions for the location you choose).
- Press "Save" to save changes to port number or storage directory.
- SCREENSHOT
### Client Side:
- Enter the username/password of the account you would like to back up to in the "username" and "password" fields.
- Press "Choose Directory" to choose the directory whose files you would like to back up.
- Enter the IP address of the backup server (such as `127.0.0.1`) into the "IP Address" field.
- Enter the port number of the backup server into the "port number" field.
- Press "Backup."
- SCREENSHOT



<p align="right">(<a href="#readme-top">back to top</a>)</p>  




<!-- LICENSE -->  
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>  



<!-- CONTACT -->  
## Contact

Visit my website at  [danielmayer.me](https://danielmayer.me) or email me at [daniel@danielmayer.me](mailto:daniel@danielmayer.me).

Project Link: [https://github.com/github_username/repo_name](https://github.com/github_username/repo_name)

<p align="right">(<a href="#readme-top">back to top</a>)</p>  





<!-- MARKDOWN LINKS & IMAGES -->  
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->  
[contributors-shield]: https://img.shields.io/github/contributors/github_username/repo_name.svg?style=for-the-badge
[contributors-url]: https://github.com/github_username/repo_name/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/github_username/repo_name.svg?style=for-the-badge
[forks-url]: https://github.com/github_username/repo_name/network/members
[stars-shield]: https://img.shields.io/github/stars/github_username/repo_name.svg?style=for-the-badge
[stars-url]: https://github.com/github_username/repo_name/stargazers
[issues-shield]: https://img.shields.io/github/issues/github_username/repo_name.svg?style=for-the-badge
[issues-url]: https://github.com/github_username/repo_name/issues
[license-shield]: https://img.shields.io/github/license/github_username/repo_name.svg?style=for-the-badge
[license-url]: https://github.com/github_username/repo_name/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/linkedin_username
[product-screenshot]: images/screenshot.png
[Next.js]: https://img.shields.io/badge/next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white
[Next-url]: https://nextjs.org/
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[Vue-url]: https://vuejs.org/
[Angular.io]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[Angular-url]: https://angular.io/
[Svelte.dev]: https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00
[Svelte-url]: https://svelte.dev/
[Laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[Laravel-url]: https://laravel.com
[Bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[Bootstrap-url]: https://getbootstrap.com
[JQuery.com]: https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white
[JQuery-url]: https://jquery.com