# compilers-2019
main repository for lab works

<b>КРАТКОЕ ОПИСАНИЕ ПРОЕКТА (Рабочее название - "ProBoost"):</b>

ПРОБЛЕМА: 
<br>Довольно проблематично найти разработчику-одиночке специалиста, который бы смог его проконсультировать, помочь 
в разработке, стать ментором и т. п. Такжеиногда бывает сложно реализовать себя как ментора, если у Вас есть опыт, но не с кем им делиться.  
<br>
РЕШЕНИЕ: 
<br>Собрать всех желающих менторов в единую базу, собрать данные об их навыках (постепенно оценивая каждый из них людьми, которые пользуются их помощью).
Собирать заявки от желающих получить квалифицированную помощь (возможно с вознаграждением: метод аукциона). 
Дать возможность одним - просматривать заявки, другим - выбирать себе ментора. Сделать систему сообщений. (Хочешь получить помощь быстрее - напиши ментору в личку).
Хочешь оказать помощь (за деньги) - пиши почему это должен быть именно ты. 
Можно добавить систему видеозвонков и/или видеотрансляций для менторов.
<br>
<br>
<b>ЯЗЫК ProBoostAL:</b>

ПРОБЛЕМА: 
Необходимо создать приложение для администрирования и поддержки данных в целостности.
Но на начальном этапе создавать такое приложение будет довольно затратно.
<br>
<br>
РЕШЕНИЕ:
Создать язык программирования, который будет способен генерировать запросы на удаленный сервер и управлять данными.
А также собирать некоторую простую статистику.
<br>
<br>
ЗАДАЧИ:
<ul>
    <li>
        Администрирование приложения
        <ul>
            <li>Удаление сущностей</li>    
            <li>Добавление/редактиррование скиллов</li>    
            <li>Добавление/редактиррование видов помощи (менторинг, консультация)</li>    
            <li>CRUD админов</li>    
            <li>Системные сообщения</li>    
            <li>Закрытие старых обьявлений</li>    
        </ul>
    </li>
    <li>
        Сбор статистики
        <ul>
            <li>Количество новых пользователей (за период времени)</li>    
            <li>Количество активных пользователей (за период времени)</li>    
            <li>Количество новых менторов (за период времени)</li>    
            <li>Количество активных мменторов (за период времени)</li>    
        </ul>
    </li>
</ul>
<br>
<br>
ПРИМЕР КОДА:

```
!connect: host("http://pro.boost.com").login("123").password("123");
      
!mentors: get(id="123").set(address="321").set(phone="0660000000").save();
          get(phone="0660000000", address="321").delete();
          add(phone="0660000000").set(about="nothing").save();
      
!users: get().min(created="27.01.11").max(created="27.02.11").print();
        get().min(logged="27.01.11").print("users.txt");
        add().max(about="nothing").count();

```
РЕЗУЛЬТАТ ВЫПОЛНЕНИЯ:

```
Analysis complete! No issues were found.
GET: http://pro.boost.com/mentors?login=123&password=123&id=123
GET: http://pro.boost.com/mentors?login=123&password=123&phone=0660000000&address=321
PUT: http://pro.boost.com/mentors?login=123&password=123
GET: http://pro.boost.com/users?login=123&password=123
[]
GET: http://pro.boost.com/users?login=123&password=123
1
```
БНФ:

```
<lowalpha>::= "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
<hialpha>::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
<alpha>::= <lowalpha> | <hialpha>
<digit>::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
<extra>::= "$" | "-" | "_" | "." | "+" | " " | "!" | "*" | "'" | "(" | ")" | "," | "{" | "}" | "|" | "\" | "^" | "~" | "[" | "]" | "`" | "<" | ">" | "#" | "%"
<alphadigit>::= <alpha> | <digit>
<symbol>::= <alphadigit> | <extra>
<digits>::= <digit>{<digit>}
<string>::= <symbol> {<symbol>}
<value>::= \"<string>\"
<param>::= <lowalpha> | "_" {<lowalpha> | "_"}
<parammvalue>::= <param>"="<value>

<port>::= <digits>
<hostnumber>::= <digits> "." <digits> "." <digits> "." <digits>
<domainlabel>::= <alphadigit> | <alphadigit> { <alphadigit> | "-" } <alphadigit>
<toplabel>::= <alpha> | <alpha> { <alphadigit> | "-" } <alphadigit>
<hostname>::= { <domainlabel> "." } <toplabel>
<host>::= <hostname> | <hostnumber>
<hostport>::= <host> [ ":" <port> ]
<httpurl>::= "http://" <hostport> { "/" <hpath>}

<connectstatement>::= "!connect"
<hostfunction>::= "host("[<httpurl>]")"
<loginfunction>::= "login("[<value>]")"
<passwordfunction>::= "password("[<value>]")"
<connectcommand>::= <hostfunction>"."<loginfunction>"."<passwordfunction>";"
<connectblock>::= <connectstatement>":"<connectcommand>[<connectcommand>]

<instance>::= "users" | "mentors" | "addresses" | "technologies" | "helpkinds" | "orders" | "reviews"
<instancestatement>::= "!"<instance>
<getfunction>::= "get("<parammvalue>{","<parammvalue>}")"
<addfunction>::= "add("<parammvalue>{","<parammvalue>}")"
<setfunction>::= "set("<parammvalue>{","<parammvalue>}")"
<filterfunction>::= "filter("<parammvalue>{","<parammvalue>}")"
<filterminfunction>::= "filtermin("<parammvalue>{","<parammvalue>}")"
<filtermaxfunction>::= "filtermax("<parammvalue>{","<parammvalue>}")"
<printfunction>::= "print("[<value>]")"
<countfunction>::= "count()"
<savefunction>::= "save()"
<deletefunction>::= "delete()"
<instancecommand>::= <getfunction> | <addfunction> {"." <setfunction> | <filterfunction> | <filterminfunction> | <filtermaxfunction>} "." <countfunction> | <savefunction> | <deletefunction> ";"
<instanceblock>::= <instancestatement>":"<instancecommand>[<instancecommand>]

<program>::= <connectblock> {<connectblock> | <instanceblock>}
```