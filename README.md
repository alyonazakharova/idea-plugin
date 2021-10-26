# idea-plugin

Инспекция, реализованная в данном плагине, предназначена для проверки того, что при создании объекта инпут класса общего или глобального шага были заданы все обязательные поля.

Обязательным полем считается поле, не имеющее аннотации @OptionalInput.

Если поле обязательное и не задано (в цепочке сеттеров не найдено соответствующее значение) => получаем предупреждение.

Если значение поля не задано и поле является обязательным (не имеет аннотации @OptionalInput), но при поле является примитивным (имеет значение по умолчанию), проверка пропускает это поле и не выдает никаких ошибок.
Исключением является примитивное поле isSigosTest/sigos/sigosTest. Если данное поле не задано => получаем предупреждение.

Если значение поля не задано, поле является обязательным и не имеет аннотации DefaultValue => получаем слабое предупреждение. Экземпляр инпут класса не выделяется желтым цветом, а только подчеркивается серым. В данном случае стоит прорефакторить инпут класс, повесить над полем аннотацию @DefaultValue и убрать сеттер, который ранее заменял данную аннотацию.

Доступно добавление недостающих сеттеров с помощью quick-fix.

Класс PrimitiveFieldsInspection проверяет, что примитивные поля не помечены аннотацией @OptionalInput.
В данном случае можно с помощью quick-fix удалить аннотацию, либо изменить тип поля (остается на совести автоматизатора).

В классе AnnotationInspection проверяется, что класс с новой реализацией (который наследуется не от BaseTest, а от продуктового BaseTest или напрямую от AbstractTest) помечен аннотацией @TC_ID. Также проверяется, что значение аннотации не пустое.
