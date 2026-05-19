-- ============================================================
-- OrdaSkills — стартовые курсы + уроки
-- Flyway: V11__seed_courses_with_lessons.sql
-- Положить в: src/main/resources/db/migration/
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- КУРСЫ
-- ─────────────────────────────────────────────────────────────
INSERT INTO courses (slug, title, subtitle, description, locale, instructor_name, level, duration_hours, price, status, created_at, updated_at) VALUES

('python-basics',
 'Python негіздері',
 'Нөлден бастап бағдарламалауды үйрен',
 'Python тілінің негізгі синтаксисін, айнымалыларды, циклдарды, функцияларды және файлдармен жұмысты үйренесіз. Тәжірибесіз адамдарға арналған курс.',
 'kk', 'OYAN Team', 'BEGINNER', 20, 9900.00, 'PUBLISHED', NOW(), NOW()),

('javascript-basics',
 'JavaScript негіздері',
 'Веб-беттерді интерактивті ету',
 'JavaScript синтаксисін, DOM-ды, оқиғаларды және fetch API-ды үйренесіз. Курстың соңында өз шағын веб-қосымшаңызды жасайсыз.',
 'kk', 'OYAN Team', 'BEGINNER', 25, 9900.00, 'PUBLISHED', NOW(), NOW()),

('react-basics',
 'React арқылы фронтенд',
 'Заманауи веб-қосымшалар жасау',
 'React компоненттерін, хуктарды (useState, useEffect), React Router және API-мен жұмысты үйренесіз. JavaScript негіздерін білетіндерге арналған.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 30, 14900.00, 'PUBLISHED', NOW(), NOW()),

('sql-basics',
 'SQL негіздері',
 'Деректер базасымен жұмыс',
 'SELECT, INSERT, UPDATE, DELETE, JOIN және GROUP BY операторларын үйренесіз. PostgreSQL-де нақты тапсырмаларды орындайсыз.',
 'kk', 'OYAN Team', 'BEGINNER', 15, 7900.00, 'PUBLISHED', NOW(), NOW()),

('ui-ux-basics',
 'UI/UX дизайн негіздері',
 'Figma арқылы интерфейс жасау',
 'Пайдаланушы интерфейсін жобалауды үйреніңіз: wireframe, прототип, дизайн жүйесі. Figma-да нақты мобильді қосымша дизайны жасалады.',
 'kk', 'OYAN Team', 'BEGINNER', 18, 8900.00, 'PUBLISHED', NOW(), NOW()),

('python-data',
 'Python арқылы деректерді талдау',
 'Pandas, NumPy және Matplotlib',
 'Нақты деректер жиындарымен жұмыс жасауды үйреніңіз. Деректерді тазалау, талдау және визуализация толық қарастырылады.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 35, 19900.00, 'PUBLISHED', NOW(), NOW()),

('django-backend',
 'Django арқылы бэкенд',
 'REST API және дерекқорлар',
 'Django фреймворкімен толыққанды веб-қосымша жасауды үйреніңіз. ORM, миграциялар, JWT аутентификациясы, Django REST Framework.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 40, 24900.00, 'PUBLISHED', NOW(), NOW()),

('flutter-mobile',
 'Flutter арқылы мобильді қосымша',
 'iOS және Android — бір кодтан',
 'Flutter және Dart тілімен кросс-платформалық мобильді қосымшалар жасауды үйреніңіз. Виджеттер, навигация және Firebase интеграциясы.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 45, 24900.00, 'PUBLISHED', NOW(), NOW()),

('docker-basics',
 'Docker негіздері',
 'Контейнерлеу және деплой',
 'Docker арқылы қосымшаларды контейнерге салуды үйреніңіз. Docker Compose, образдар, желілер және CI/CD интеграциясы толық қарастырылады.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 20, 12900.00, 'PUBLISHED', NOW(), NOW()),

('git-basics',
 'Git және GitHub негіздері',
 'Командамен жұмыс жасауды үйрен',
 'Git-тің негізгі командаларын, branch, merge, pull request және GitHub арқылы командалық жұмысты үйренесіз. Барлық деңгейге арналған.',
 'kk', 'OYAN Team', 'BEGINNER', 10, 4900.00, 'PUBLISHED', NOW(), NOW()),

('machine-learning-intro',
 'Машиналық оқытуға кіріспе',
 'Scikit-learn арқылы ML модельдері',
 'Машиналық оқытудың негізгі алгоритмдерін үйреніңіз: сызықтық регрессия, шешім ағаштары, кластерлеу. Scikit-learn арқылы нақты деректерде модельдер жасалады.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 40, 22900.00, 'PUBLISHED', NOW(), NOW()),

('cybersecurity-intro',
 'Ақпараттық қауіпсіздік негіздері',
 'Желі қауіпсіздігі және этикалық хакинг',
 'Ақпараттық қауіпсіздіктің негіздерін үйреніңіз: желі протоколдары, шифрлау, веб-қосымша осалдықтары (OWASP Top 10) және Kali Linux.',
 'kk', 'OYAN Team', 'BEGINNER', 25, 14900.00, 'PUBLISHED', NOW(), NOW()),

('nextjs-course',
 'Next.js арқылы фулстек',
 'SSR, App Router және деплой',
 'Next.js фреймворкімен серверлік рендеринг, API роуттар, Prisma ORM, аутентификация және Vercel-ге деплой жасауды үйреніңіз.',
 'kk', 'OYAN Team', 'ADVANCED', 50, 29900.00, 'PUBLISHED', NOW(), NOW()),

('typescript-basics',
 'TypeScript негіздері',
 'JavaScript-ті типтермен күшейт',
 'TypeScript-тің негізгі синтаксисін, интерфейстерді, generic-тарды және React-пен бірге қолдануды үйренесіз.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 20, 11900.00, 'PUBLISHED', NOW(), NOW()),

('aws-basics',
 'AWS бұлтты қызметтер',
 'EC2, S3, RDS және Lambda',
 'Amazon Web Services негізгі қызметтерін практикамен үйреніңіз: EC2, S3, RDS, Lambda, IAM және CloudWatch.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 30, 19900.00, 'PUBLISHED', NOW(), NOW()),

('figma-advanced',
 'Figma: кәсіби деңгей',
 'Auto Layout, компоненттер және дизайн жүйесі',
 'Figma-ның кәсіби мүмкіндіктерін үйреніңіз: Auto Layout, компонент варианттары, дизайн токендары және командамен синхронды жұмыс.',
 'kk', 'OYAN Team', 'ADVANCED', 22, 13900.00, 'PUBLISHED', NOW(), NOW()),

('node-express',
 'Node.js және Express',
 'Серверлік JavaScript',
 'Node.js арқылы серверлік қосымша жасауды үйреніңіз. Express, middleware, REST API, JWT аутентификациясы және MongoDB-мен жұмыс.',
 'kk', 'OYAN Team', 'INTERMEDIATE', 30, 16900.00, 'PUBLISHED', NOW(), NOW()),

('agile-scrum',
 'Agile және Scrum негіздері',
 'IT жобаларын сәтті жеткізу',
 'Agile методологиясы мен Scrum фреймворкінің барлық аспектілерін үйреніңіз: спринт жоспарлауы, ретроспектива, Kanban.',
 'kk', 'OYAN Team', 'BEGINNER', 12, 6900.00, 'PUBLISHED', NOW(), NOW()),

('linux-basics',
 'Linux негіздері',
 'Командалық жол арқылы жұмыс',
 'Linux операциялық жүйесінің негіздерін үйреніңіз: терминал командалары, файл жүйесі, рұқсаттар, bash скриптері және серверді баптау.',
 'kk', 'OYAN Team', 'BEGINNER', 15, 7900.00, 'PUBLISHED', NOW(), NOW()),

('english-it',
 'IT-ге арналған ағылшын тілі',
 'Техникалық ағылшын тілін меңгер',
 'IT саласына арналған ағылшын тілін үйреніңіз: техникалық терминология, документация оқу, pull request жазу және код ревью жасау.',
 'kk', 'OYAN Team', 'BEGINNER', 20, 9900.00, 'PUBLISHED', NOW(), NOW());

-- ─────────────────────────────────────────────────────────────
-- УРОКИ — әр курсқа 4-5 сабақ
-- content: қарапайым мәтін (жұмыс істейді)
-- video_url: YouTube embed (жұмыс істейді)
-- ─────────────────────────────────────────────────────────────

-- python-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-python-intro', 'Python-ға кіріспе', 'Python дегеніміз не және оны қалай орнатамыз',
'Python — қарапайым синтаксисі бар жоғары деңгейлі программалау тілі. 1991 жылы Гвидо ван Россум жасаған. Веб-дамыту, деректерді талдау, жасанды интеллект салаларында кеңінен қолданылады. Бүгін Python-ды орнатып, алғашқы "Hello, World!" программамызды жазамыз.',
'https://www.youtube.com/watch?v=_uQrJ0TkZlc', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'python-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-python-variables', 'Айнымалылар және деректер түрлері', 'int, float, str, bool — негізгі типтер',
'Айнымалы — деректерді сақтайтын жады ұяшығы. Python-да айнымалы жариялау үшін тип көрсетудің қажеті жоқ. Мысал: name = "Aibek", age = 20, height = 1.75, is_student = True. Деректер түрлері: int (бүтін), float (бөлшек), str (мәтін), bool (логикалық).',
'https://www.youtube.com/watch?v=khKv-8q7YmY', 2, 18, NOW(), NOW() FROM courses WHERE slug = 'python-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-python-conditions', 'Шарттар және if-else', 'Бағдарламаға шешім қабылдауды үйрету',
'Шарттар операторы бағдарламаның белгілі бір жағдайда әртүрлі әрекет жасауына мүмкіндік береді. if шарт: ... elif басқа_шарт: ... else: ... Мысал: if age >= 18: print("Кәмелет жасқа толған") else: print("Кәмелет жасқа толмаған").',
'https://www.youtube.com/watch?v=DZwmZ8Usvnk', 3, 15, NOW(), NOW() FROM courses WHERE slug = 'python-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '4-python-loops', 'Циклдар: for және while', 'Қайталанатын әрекеттерді автоматтандыру',
'Цикл — белгілі бір кодты бірнеше рет орындау құралы. for i in range(5): print(i) — 0-ден 4-ке дейін санайды. while шарт: ... — шарт ақиқат болса орындала береді. break — циклды тоқтату, continue — келесі итерацияға өту.',
'https://www.youtube.com/watch?v=6iF8Xb7Z3wQ', 4, 18, NOW(), NOW() FROM courses WHERE slug = 'python-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '5-python-functions', 'Функциялар', 'Кодты қайта пайдаланылатын блоктарға бөлу',
'Функция — белгілі бір тапсырманы орындайтын код блогы. def функция_аты(параметрлер): ... return нәтиже. Мысал: def greet(name): return f"Сәлем, {name}!" Функциялар кодты қысқартады, оқылымдылықты арттырады және қайта пайдалануға мүмкіндік береді.',
'https://www.youtube.com/watch?v=9Os0o3wzS_I', 5, 20, NOW(), NOW() FROM courses WHERE slug = 'python-basics';

-- javascript-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-js-intro', 'JavaScript-ке кіріспе', 'Браузерде JS қалай жұмыс істейді',
'JavaScript — веб-беттерді интерактивті ету үшін қолданылатын бағдарламалау тілі. Браузерде тікелей орындалады. HTML мазмұнын өзгерту, пайдаланушы оқиғаларына жауап беру, серверге сұраулар жіберу — бәрі JS арқылы жасалады.',
'https://www.youtube.com/watch?v=W6NZfCO5SIk', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'javascript-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-js-variables', 'Айнымалылар: var, let, const', 'Заманауи JS-де айнымалыларды дұрыс жариялау',
'JS-де айнымалы жариялаудың 3 жолы бар: var (ескі), let (өзгеретін), const (өзгермейтін). Заманауи JS-де var қолданбаңыз. let name = "Aibek"; const PI = 3.14; Айнымалы атаулары camelCase форматында жазылады.',
'https://www.youtube.com/watch?v=9emXNzqCKyg', 2, 18, NOW(), NOW() FROM courses WHERE slug = 'javascript-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-js-dom', 'DOM манипуляциясы', 'HTML элементтерін JS арқылы өзгерту',
'DOM (Document Object Model) — HTML-дің JS-тен қолжетімді ағаш тәрізді құрылымы. document.getElementById("id") — элементті id бойынша табу. element.textContent = "мәтін" — мазмұнды өзгерту. element.style.color = "red" — стильді өзгерту.',
'https://www.youtube.com/watch?v=0ik6X4DJKCc', 3, 22, NOW(), NOW() FROM courses WHERE slug = 'javascript-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '4-js-events', 'Оқиғалар (Events)', 'Пайдаланушы әрекеттеріне жауап беру',
'Оқиға — пайдаланушының немесе браузердің белгілі бір әрекеті: click, keypress, submit, load. addEventListener арқылы оқиғаны тыңдаймыз: button.addEventListener("click", function() { alert("Басылды!"); }). Event object арқылы оқиға туралы ақпарат аламыз.',
'https://www.youtube.com/watch?v=XF1_MlZ5l6M', 4, 20, NOW(), NOW() FROM courses WHERE slug = 'javascript-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '5-js-fetch', 'Fetch API және асинхронды код', 'Серверден деректер алу',
'Fetch API серверге HTTP сұраулар жіберуге мүмкіндік береді. fetch(url).then(res => res.json()).then(data => console.log(data)). async/await синтаксисі кодты оқылымды етеді: const data = await fetch(url).then(r => r.json()). Promise, async, await — асинхронды JS-тің негізі.',
'https://www.youtube.com/watch?v=cuEtnrL9-H0', 5, 25, NOW(), NOW() FROM courses WHERE slug = 'javascript-basics';

-- react-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-react-intro', 'React-қа кіріспе', 'Неліктен React және оны қалай орнатамыз',
'React — Facebook жасаған UI кітапханасы. Компонентке негізделген архитектура, virtual DOM, SPA жасауға ыңғайлы. npx create-react-app my-app командасымен жоба жасаймыз. JSX — HTML-ге ұқсас синтаксис, JavaScript-тің ішінде қолданылады.',
'https://www.youtube.com/watch?v=Ke90Tje7VS0', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'react-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-react-components', 'Компоненттер және props', 'React-тың негізгі құрылым блоктары',
'Компонент — UI-дің қайта пайдаланылатын бөлігі. Function компоненті: function Button({ label }) { return <button>{label}</button>; } Props арқылы деректерді компонентке жіберіп, динамикалық UI жасаймыз. Компоненттер кішіден үлкенге қарай жасалады.',
'https://www.youtube.com/watch?v=jLS0TkAHvRg', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'react-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-react-state', 'useState хуктары', 'Компонент күйін басқару',
'useState — компонент ішіндегі деректерді сақтауға арналған хук. const [count, setCount] = useState(0); Күй өзгергенде React компонентті қайта рендерлейді. Форма деректерін, модальдарды, санауыштарды useState арқылы басқарамыз.',
'https://www.youtube.com/watch?v=-MlNBTSg_Ww', 3, 20, NOW(), NOW() FROM courses WHERE slug = 'react-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '4-react-effect', 'useEffect хуктары', 'Жанама әсерлерді басқару',
'useEffect — компонент рендерленгеннен кейін орындалатын хук. API сұраулары, подпискалар, таймерлар — бәрі useEffect ішінде жасалады. useEffect(() => { fetchData(); }, []) — тәуелділік массиві бос болса, тек бір рет орындалады.',
'https://www.youtube.com/watch?v=0ZJgIjIuY7U', 4, 22, NOW(), NOW() FROM courses WHERE slug = 'react-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '5-react-router', 'React Router арқылы навигация', 'Бет-бетке өту логикасы',
'React Router — SPA-да беттер арасында навигация жасауға арналған кітапхана. BrowserRouter, Routes, Route компоненттері. Link арқылы беттер арасында өтеміз. useNavigate — программалық навигация. useParams — URL параметрлерін оқу.',
'https://www.youtube.com/watch?v=Law7wfdg_ls', 5, 25, NOW(), NOW() FROM courses WHERE slug = 'react-basics';

-- sql-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-sql-intro', 'SQL-ға кіріспе', 'Дерекқор дегеніміз не',
'SQL (Structured Query Language) — реляциялық дерекқорлармен жұмыс жасауға арналған тіл. Кесте (table), жол (row), баған (column) — негізгі ұғымдар. PostgreSQL, MySQL, SQLite — танымал RDBMS жүйелері. Бүгін PostgreSQL орнатып, алғашқы кестемізді жасаймыз.',
'https://www.youtube.com/watch?v=HXV3zeQKqGY', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'sql-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-sql-select', 'SELECT сұраулары', 'Деректерді оқу және сүзу',
'SELECT — деректерді оқуға арналған оператор. SELECT * FROM users; — барлық бағандарды оқу. SELECT name, email FROM users WHERE age > 18; — шартпен сүзу. ORDER BY age DESC — сұрыптау. LIMIT 10 — санмен шектеу. WHERE ішінде AND, OR, NOT, LIKE, IN қолданылады.',
'https://www.youtube.com/watch?v=YufocuHbYZo', 2, 18, NOW(), NOW() FROM courses WHERE slug = 'sql-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-sql-join', 'JOIN операциялары', 'Кестелерді біріктіру',
'JOIN — бірнеше кестедегі деректерді біріктіруге мүмкіндік береді. INNER JOIN — екі кестеде де сәйкес жол болса қосады. LEFT JOIN — сол кестенің барлық жолдарын қосады. Мысал: SELECT u.name, o.product FROM users u JOIN orders o ON u.id = o.user_id;',
'https://www.youtube.com/watch?v=9yeOJ0ZMUYw', 3, 22, NOW(), NOW() FROM courses WHERE slug = 'sql-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '4-sql-insert-update', 'INSERT, UPDATE, DELETE', 'Деректерді қосу, өзгерту, жою',
'INSERT INTO users (name, email) VALUES ("Aibek", "aibek@mail.com"); — деректер қосу. UPDATE users SET name = "Aigerim" WHERE id = 1; — деректерді өзгерту. DELETE FROM users WHERE id = 1; — деректерді жою. WHERE-сіз UPDATE/DELETE бүкіл кестені өзгертеді — абай болыңыз!',
'https://www.youtube.com/watch?v=p3qvj9hO_Bo', 4, 15, NOW(), NOW() FROM courses WHERE slug = 'sql-basics';

-- ui-ux-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-ux-intro', 'UX дизайнға кіріспе', 'Пайдаланушы тәжірибесі дегеніміз не',
'UX (User Experience) — пайдаланушының өнімді пайдалану кезіндегі толық тәжірибесі. Жақсы UX = пайдаланушы мақсатына жеңіл жетеді. UI (User Interface) — интерфейстің визуалды бөлігі. UX зерттеуден басталады: кім қолданады? Не қажет? Қандай мәселелер бар?',
'https://www.youtube.com/watch?v=t0aCoqXKFOU', 1, 18, NOW(), NOW() FROM courses WHERE slug = 'ui-ux-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-figma-intro', 'Figma-ға кіріспе', 'Figma интерфейсімен танысу',
'Figma — браузерде жұмыс жасайтын дизайн құралы. Frame, Group, Component — негізгі элементтер. Auto Layout — элементтерді автоматты орналастыру. Figma-да командамен бір уақытта жұмыс жасауға болады. Жобаны қалай жасаймыз және файлдарды қалай ұйымдастырамыз.',
'https://www.youtube.com/watch?v=FTFaQWZBqQ8', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'ui-ux-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-wireframe', 'Wireframe жасау', 'Идеяны сызбаға түсіру',
'Wireframe — интерфейстің қара-ақ сызбасы, детальсыз. Мақсаты: орналасуды жоспарлау, контентті анықтау. Low-fidelity (қарандаш сызбасы) vs High-fidelity (Figma-да толық). Wireframe жасағанда мазмұнға, орналасуға назар аударамыз, түс пен стильге емес.',
'https://www.youtube.com/watch?v=qpH7-KFWZRI', 3, 20, NOW(), NOW() FROM courses WHERE slug = 'ui-ux-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '4-prototype', 'Прототип жасау', 'Интерактивті макет',
'Прототип — жұмыс жасайтын макет, код жоқ. Figma-да беттер арасында байланыс жасаймыз: Prototype панелі, Connection, Interaction. Пайдаланушыларға тестілеуге береміыз. Прототип арқылы идеяны дамытушыларға дейін тексеруге болады.',
'https://www.youtube.com/watch?v=QL5FOP_ZXNE', 4, 22, NOW(), NOW() FROM courses WHERE slug = 'ui-ux-basics';

-- python-data
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-pandas-intro', 'Pandas кіріспе', 'DataFrame және Series',
'Pandas — Python-да деректерді талдауға арналған кітапхана. DataFrame — кестелік деректер. Series — бір өлшемді деректер. import pandas as pd. pd.read_csv("file.csv") — CSV оқу. df.head() — алғашқы 5 жол. df.info() — деректер туралы ақпарат. df.describe() — статистика.',
'https://www.youtube.com/watch?v=vmEHCJofslg', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'python-data';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-pandas-clean', 'Деректерді тазалау', 'NaN, дубликаттар, қате типтер',
'Нақты деректер әрқашан таза болмайды. df.isnull().sum() — бос мәндерді санау. df.dropna() — бос жолдарды жою. df.fillna(0) — бос мәндерді толтыру. df.duplicated() — дубликаттарды табу. df["age"] = df["age"].astype(int) — типті өзгерту.',
'https://www.youtube.com/watch?v=bDhvCp3_lYw', 2, 28, NOW(), NOW() FROM courses WHERE slug = 'python-data';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-matplotlib', 'Matplotlib арқылы визуализация', 'Графиктер мен диаграммалар',
'Matplotlib — Python-да деректерді визуализациялауға арналған кітапхана. import matplotlib.pyplot as plt. plt.plot() — сызықтық график. plt.bar() — баған диаграммасы. plt.scatter() — шашырау диаграммасы. plt.hist() — гистограмма. plt.show() — графикті көрсету.',
'https://www.youtube.com/watch?v=3Xc3CA655Y4', 3, 22, NOW(), NOW() FROM courses WHERE slug = 'python-data';

-- django-backend
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-django-intro', 'Django-ға кіріспе', 'MVT архитектурасы',
'Django — Python-дағы толыққанды веб-фреймворк. MVT (Model-View-Template) архитектурасы. pip install django. django-admin startproject mysite. python manage.py runserver — серверді іске қосу. Django-ның кіріктірілген admin панелі, ORM, аутентификация жүйесі бар.',
'https://www.youtube.com/watch?v=F5mRW0jo-U4', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'django-backend';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-django-models', 'Модельдер және ORM', 'Дерекқормен жұмыс',
'Model — дерекқор кестесін Python класы ретінде сипаттайды. class Course(models.Model): title = models.CharField(max_length=200). python manage.py makemigrations && migrate — кесте жасау. ORM арқылы SQL жазбай-ақ деректерді CRUD операцияларын орындаймыз.',
'https://www.youtube.com/watch?v=PD3YnPSKx-E', 2, 28, NOW(), NOW() FROM courses WHERE slug = 'django-backend';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-django-rest', 'Django REST Framework', 'API жасау',
'DRF — Django-да REST API жасауға арналған кітапхана. Serializer — модель деректерін JSON-ға айналдырады. ViewSet — CRUD операцияларын автоматты жасайды. Router — URL-дерді автоматты тіркейді. pip install djangorestframework.',
'https://www.youtube.com/watch?v=TmsD8QOptUM', 3, 30, NOW(), NOW() FROM courses WHERE slug = 'django-backend';

-- flutter-mobile
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-flutter-intro', 'Flutter-ге кіріспе', 'Dart тілі және Flutter орнату',
'Flutter — Google жасаған кросс-платформалық UI фреймворк. Dart тілінде жазылады. Бір кодтан iOS, Android, Web, Desktop қосымшалары жасалады. flutter create my_app. flutter run — эмуляторда іске қосу. Hot Reload — кодты өзгертіп, нәтижені бірден көру.',
'https://www.youtube.com/watch?v=1ukSR1GRtMU', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'flutter-mobile';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-flutter-widgets', 'Виджеттер', 'Flutter-дің негізгі блоктары',
'Flutter-де бәрі виджет: мәтін, батырма, сурет, орналасу — бәрі виджет. StatelessWidget — күйсіз виджет. StatefulWidget — күйі бар виджет. Негізгі виджеттер: Text, Container, Row, Column, Stack, Image, ElevatedButton.',
'https://www.youtube.com/watch?v=TSIhiZ5yRos', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'flutter-mobile';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-flutter-navigation', 'Навигация', 'Беттер арасында өту',
'Flutter-де Navigator арқылы беттер арасында өтеміз. Navigator.push() — жаңа бет ашу. Navigator.pop() — артқа қайту. Named routes — атаулы маршруттар. go_router пакеті — заманауи навигация шешімі.',
'https://www.youtube.com/watch?v=nyvwx7o277U', 3, 20, NOW(), NOW() FROM courses WHERE slug = 'flutter-mobile';

-- docker-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-docker-intro', 'Docker-ге кіріспе', 'Контейнерлеу дегеніміз не',
'Docker — қосымшаны оның тәуелділіктерімен бірге контейнерге орап жеткізу құралы. "Менің компьютерімде жұмыс істейді" мәселесін шешеді. Image — контейнердің шаблоны. Container — жұмыс жасаушы image данасы. docker run hello-world — алғашқы контейнер.',
'https://www.youtube.com/watch?v=3c-iBn73dDE', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'docker-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-dockerfile', 'Dockerfile жасау', 'Өз image-ді жасау',
'Dockerfile — image жасауға нұсқаулар файлы. FROM node:18 — базалық image. WORKDIR /app — жұмыс каталогы. COPY . . — файлдарды көшіру. RUN npm install — командаларды орындау. EXPOSE 3000 — портты ашу. CMD ["node", "index.js"] — іске қосу командасы.',
'https://www.youtube.com/watch?v=SnSH8Ht3MIc', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'docker-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-docker-compose', 'Docker Compose', 'Бірнеше контейнерді басқару',
'Docker Compose — бірнеше контейнерді бірге іске қосу құралы. docker-compose.yml файлында сервистерді сипаттаймыз: web, db, redis. docker compose up — барлық сервисті іске қосу. docker compose down — тоқтату. Volumes арқылы деректерді сақтаймыз.',
'https://www.youtube.com/watch?v=HG6yIjZapSA', 3, 25, NOW(), NOW() FROM courses WHERE slug = 'docker-basics';

-- git-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-git-intro', 'Git-ке кіріспе', 'Нұсқаларды басқару жүйесі',
'Git — код өзгерістерін қадағалайтын нұсқаларды басқару жүйесі. Неліктен Git: тарихты сақтайды, командамен жұмыс жасайды, қателерді қайтарады. git init — репозиторий жасау. git status — өзгерістерді көру. git add . — файлдарды индексқа қосу. git commit -m "message".',
'https://www.youtube.com/watch?v=RGOj5yH7evk', 1, 18, NOW(), NOW() FROM courses WHERE slug = 'git-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-git-branch', 'Branches және Merge', 'Параллельді жұмыс жасау',
'Branch — негізгі кодтан бөлек жұмыс жасауға арналған тармақ. git branch feature/login — жаңа тармақ. git checkout feature/login — тармаққа өту. git merge feature/login — тармақтарды біріктіру. Merge conflict — бір файлды екі адам өзгерткенде туындайды.',
'https://www.youtube.com/watch?v=e2IbNHi4uCI', 2, 20, NOW(), NOW() FROM courses WHERE slug = 'git-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-github-pr', 'GitHub және Pull Request', 'Командамен жұмыс',
'GitHub — Git репозиторийлерін бұлтта сақтауға арналған платформа. git remote add origin URL — GitHub-пен байланыстыру. git push origin main — кодты жүктеу. Pull Request — кодды main-ге қосу өтінімі. Code Review — командадастың кодыңызды тексеруі.',
'https://www.youtube.com/watch?v=For9VtrQx58', 3, 18, NOW(), NOW() FROM courses WHERE slug = 'git-basics';

-- machine-learning-intro
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-ml-intro', 'Машиналық оқытуға кіріспе', 'ML дегеніміз не және қолдану салалары',
'Машиналық оқыту — компьютерлерге деректерден үйренуді үйрету ғылымы. Supervised (бақыланатын), Unsupervised (бақыланбайтын), Reinforcement learning. Қолдану: суретті тану, спам сүзу, баға болжау, ұсынымдар. Python + scikit-learn — ML-дің ең танымал тандемі.',
'https://www.youtube.com/watch?v=NWONeJKn6kc', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'machine-learning-intro';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-ml-regression', 'Сызықтық регрессия', 'Сандарды болжау',
'Сызықтық регрессия — сандық мән болжайтын алгоритм. Үй бағасын, температураны болжауға қолданылады. from sklearn.linear_model import LinearRegression. model.fit(X_train, y_train) — үйрету. model.predict(X_test) — болжау. R² score — модель сапасын өлшейді.',
'https://www.youtube.com/watch?v=NUXdtN1W1FE', 2, 28, NOW(), NOW() FROM courses WHERE slug = 'machine-learning-intro';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-ml-classification', 'Классификация', 'Категорияны анықтау',
'Классификация — объектті санатқа жатқызатын алгоритм. Spam/not spam, ауру/сау. Logistic Regression, Decision Tree, Random Forest. from sklearn.tree import DecisionTreeClassifier. Accuracy, Precision, Recall, F1-score — классификация метрикалары.',
'https://www.youtube.com/watch?v=LDRbO9a6XPU', 3, 25, NOW(), NOW() FROM courses WHERE slug = 'machine-learning-intro';

-- cybersecurity-intro
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-cyber-intro', 'Ақпараттық қауіпсіздікке кіріспе', 'CIA триадасы',
'Ақпараттық қауіпсіздік — деректерді рұқсатсыз кіруден, өзгертуден, жоюдан қорғау. CIA триадасы: Confidentiality (құпиялылық), Integrity (тұтастық), Availability (қолжетімділік). Threat, Vulnerability, Risk — негізгі ұғымдар. Black hat vs White hat хакерлер.',
'https://www.youtube.com/watch?v=inWWhr5tnEA', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'cybersecurity-intro';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-cyber-network', 'Желі қауіпсіздігі', 'Протоколдар және шабуылдар',
'TCP/IP, HTTP/HTTPS, DNS, SSH — негізгі желі протоколдары. Man-in-the-Middle, DDoS, SQL Injection, XSS — жиі кездесетін шабуылдар. Firewall, IDS/IPS, VPN — қорғану құралдары. Wireshark арқылы желі трафигін талдаймыз.',
'https://www.youtube.com/watch?v=qiQR5rTSshw', 2, 25, NOW(), NOW() FROM courses WHERE slug = 'cybersecurity-intro';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-cyber-owasp', 'OWASP Top 10', 'Веб-қосымша осалдықтары',
'OWASP Top 10 — веб-қосымшалардың ең қауіпті 10 осалдығы. SQL Injection: пайдаланушы енгізімі арқылы SQL командасы жіберу. XSS: зиянды JavaScript кодын бетке енгізу. CSRF: пайдаланушы атынан сұрау жіберу. Broken Authentication: аутентификация осалдықтары.',
'https://www.youtube.com/watch?v=t2GqXMrpfNE', 3, 22, NOW(), NOW() FROM courses WHERE slug = 'cybersecurity-intro';

-- nextjs-course
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-nextjs-intro', 'Next.js-ке кіріспе', 'App Router және жоба құрылымы',
'Next.js — React-қа негізделген фулстек фреймворк. SSR (Server-Side Rendering), SSG (Static Site Generation), ISR. npx create-next-app@latest. App Router — жаңа маршруттау жүйесі. app/page.tsx — бас бет. app/about/page.tsx — /about беті.',
'https://www.youtube.com/watch?v=wm5gMKuwSYk', 1, 25, NOW(), NOW() FROM courses WHERE slug = 'nextjs-course';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-nextjs-routing', 'Маршруттау және беттер', 'Динамикалық маршруттар',
'Next.js-те файл жүйесі арқылы маршруттар жасалады. app/courses/[slug]/page.tsx — динамикалық бет. useParams() — URL параметрлерін оқу. generateStaticParams() — статикалық беттерді алдын ала жасау. Nested layouts — орналасуларды кірістіру.',
'https://www.youtube.com/watch?v=vwSlYG7hFk0', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'nextjs-course';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-nextjs-api', 'API Routes', 'Бэкенд логикасы',
'Next.js-те app/api/route.ts арқылы API эндпоинттарын жасаймыз. GET, POST, PUT, DELETE handler функциялары. Request, Response объектілері. Prisma ORM-мен дерекқорға қосылу. NextAuth.js арқылы аутентификация жасау.',
'https://www.youtube.com/watch?v=wIedqKnRHgE', 3, 28, NOW(), NOW() FROM courses WHERE slug = 'nextjs-course';

-- typescript-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-ts-intro', 'TypeScript-ке кіріспе', 'Неліктен TypeScript',
'TypeScript — JavaScript-тің типтелген супержиыны. Компиляция кезінде қателерді табады. npm install -D typescript. tsc --init — конфигурация файлы. let name: string = "Aibek"; let age: number = 20; TypeScript кодты жазуды, оқуды, debug жасауды жеңілдетеді.',
'https://www.youtube.com/watch?v=BCg4U1FzODs', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'typescript-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-ts-types', 'Типтер және интерфейстер', 'Күрделі типтерді сипаттау',
'Interface — объект пішінін сипаттайды. interface User { name: string; age: number; } Type alias — type Point = { x: number; y: number; }. Union types: string | number. Generic types: function identity<T>(arg: T): T. Partial, Required, Pick, Omit — утилиталық типтер.',
'https://www.youtube.com/watch?v=ydkQlJhodio', 2, 22, NOW(), NOW() FROM courses WHERE slug = 'typescript-basics';

-- aws-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-aws-intro', 'AWS-ке кіріспе', 'Бұлтты есептеу негіздері',
'AWS (Amazon Web Services) — әлемдегі ең ірі бұлтты платформа. IaaS, PaaS, SaaS — бұлтты қызмет модельдері. AWS Free Tier — тегін деңгей. IAM — Identity and Access Management, пайдаланушылар мен рұқсаттарды басқару. AWS Console арқылы жұмыс.',
'https://www.youtube.com/watch?v=a9__D53WsUs', 1, 22, NOW(), NOW() FROM courses WHERE slug = 'aws-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-aws-ec2-s3', 'EC2 және S3', 'Виртуалды сервер және файл сақтау',
'EC2 (Elastic Compute Cloud) — виртуалды сервер. Instance типтері: t2.micro (Free Tier). Security Group — желілік қауіпсіздік ережелері. S3 (Simple Storage Service) — объектілік файл сақтау. Bucket жасау, файл жүктеу, статикалық сайт хостингі.',
'https://www.youtube.com/watch?v=IHX-ztKy098', 2, 25, NOW(), NOW() FROM courses WHERE slug = 'aws-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-aws-lambda', 'Lambda және API Gateway', 'Serverless архитектура',
'Lambda — серверсіз функциялар, тек сұрау кезінде орындалады. Оқиғаға жауап ретінде іске қосылады. API Gateway + Lambda = серверсіз REST API. Бағасы: орындалу санына қарай төленеді. RDS — реляциялық дерекқор қызметі.',
'https://www.youtube.com/watch?v=eOBq__h4OJ4', 3, 22, NOW(), NOW() FROM courses WHERE slug = 'aws-basics';

-- figma-advanced
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-figma-autolayout', 'Auto Layout', 'Адаптивті дизайн жасау',
'Auto Layout — элементтерді автоматты орналастыратын Figma функциясы. Horizontal, Vertical, Wrap режимдері. Padding, Gap, Alignment параметрлері. Auto Layout арқылы жасалған компоненттер мазмұнға бейімделеді. Responsive дизайн жасаудың негізі.',
'https://www.youtube.com/watch?v=TyaGpGDFczw', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'figma-advanced';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-figma-components', 'Компоненттер және варианттар', 'Дизайн жүйесінің негізі',
'Component — қайта пайдаланылатын дизайн элементі. Main component vs Instance. Variants — бір компоненттің түрлі күйлері (hover, active, disabled). Component properties — компонент параметрлері. Nested components — компоненттерді кірістіру.',
'https://www.youtube.com/watch?v=k74IrUNaJVk', 2, 25, NOW(), NOW() FROM courses WHERE slug = 'figma-advanced';

-- node-express
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-node-intro', 'Node.js-ке кіріспе', 'Серверлік JavaScript',
'Node.js — браузерден тысқары JavaScript орындайтын орта. V8 движогі. npm — пакет менеджері. npm init — жоба жасау. const http = require("http") — HTTP модулі. Event Loop — Node.js-тің асинхронды табиғатының негізі.',
'https://www.youtube.com/watch?v=TlB_eWDSMt4', 1, 22, NOW(), NOW() FROM courses WHERE slug = 'node-express';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-express-basics', 'Express.js негіздері', 'Веб-сервер жасау',
'Express — Node.js үшін минималист веб-фреймворк. npm install express. app.get("/", (req, res) => res.send("Hello")). Маршруттар: GET, POST, PUT, DELETE. Middleware — сұрауды өңдеу тізбегі. app.listen(3000) — серверді іске қосу.',
'https://www.youtube.com/watch?v=L72fhGm1tfE', 2, 25, NOW(), NOW() FROM courses WHERE slug = 'node-express';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-express-api', 'REST API жасау', 'CRUD операциялары',
'REST API принциптері: ресурстар, HTTP методтары, статус кодтары. GET /users — тізімді алу. POST /users — жаңа жасау. PUT /users/:id — өзгерту. DELETE /users/:id — жою. JSON парсинг: express.json() middleware. Postman арқылы API тестілеу.',
'https://www.youtube.com/watch?v=pKd0Rpw7O48', 3, 25, NOW(), NOW() FROM courses WHERE slug = 'node-express';

-- agile-scrum
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-agile-intro', 'Agile-ға кіріспе', 'Икемді жоба менеджменті',
'Agile — икемді жоба менеджменті методологиясы. Waterfall vs Agile. Agile манифесті 4 құндылық пен 12 принципі. Жиі жеткізу, өзгерістерге бейімделу, тапсырыс берушімен тығыз ынтымақтастық — Agile-дің негізі.',
'https://www.youtube.com/watch?v=502ILHjX9EE', 1, 15, NOW(), NOW() FROM courses WHERE slug = 'agile-scrum';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-scrum-basics', 'Scrum негіздері', 'Рөлдер, оқиғалар, артефакттар',
'Scrum — Agile-дың ең танымал фреймворкі. Рөлдер: Product Owner, Scrum Master, Development Team. Артефакттар: Product Backlog, Sprint Backlog, Increment. Оқиғалар: Sprint Planning, Daily Scrum, Sprint Review, Retrospective. Sprint — 1-4 апталық итерация.',
'https://www.youtube.com/watch?v=9TycLR0TqFA', 2, 18, NOW(), NOW() FROM courses WHERE slug = 'agile-scrum';

-- linux-basics
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-linux-intro', 'Linux-қа кіріспе', 'Терминалмен алғашқы танысу',
'Linux — ашық бастапқы кодты операциялық жүйе. Ubuntu, CentOS, Debian — танымал дистрибутивтер. Терминал — мәтіндік интерфейс. pwd — ағымдағы каталог. ls — файлдарды тізімдеу. cd — каталогты өзгерту. mkdir — каталог жасау. rm — файлды жою.',
'https://www.youtube.com/watch?v=SkB-eRCzWIU', 1, 20, NOW(), NOW() FROM courses WHERE slug = 'linux-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-linux-permissions', 'Рұқсаттар және пайдаланушылар', 'chmod, chown, sudo',
'Linux-та әр файлдың иесі және рұқсаттары болады. rwx — read, write, execute. chmod 755 file — рұқсаттарды өзгерту. chown user:group file — иесін өзгерту. sudo — суперпайдаланушы атынан орындау. /etc/passwd — пайдаланушылар тізімі.',
'https://www.youtube.com/watch?v=19SuXAQkB-E', 2, 18, NOW(), NOW() FROM courses WHERE slug = 'linux-basics';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-linux-bash', 'Bash скриптері', 'Тапсырмаларды автоматтандыру',
'Bash script — командалар тізбегін автоматты орындайтын файл. #!/bin/bash — shebang жолы. Айнымалылар, шарттар, циклдар. for f in *.log; do rm $f; done — барлық логтарды жою. cron — тапсырмаларды кесте бойынша іске қосу.',
'https://www.youtube.com/watch?v=tK9Oc6AEnR4', 3, 20, NOW(), NOW() FROM courses WHERE slug = 'linux-basics';

-- english-it
INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '1-english-tech-terms', 'IT терминологиясы', 'Негізгі техникалық сөздік',
'IT саласындағы ең жиі қолданылатын ағылшын терминдерін үйренеміз: repository, deployment, debugging, refactoring, scalability, latency, throughput, authentication, authorization. Терминдерді контексте қолдануды практикалаймыз.',
'https://www.youtube.com/watch?v=7fBuJx9tSfk', 1, 18, NOW(), NOW() FROM courses WHERE slug = 'english-it';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '2-english-docs', 'Документация оқу', 'README, API docs, Stack Overflow',
'Техникалық документацияны тиімді оқу стратегиялары. README файлын дұрыс оқу. API документациясынан мысалдар табу. Stack Overflow-дан жауап іздеу. GitHub issue-ларды оқу. Техникалық мәтіндегі белгісіз сөздерді контекстен болжау.',
'https://www.youtube.com/watch?v=2hMSDjtYvGI', 2, 20, NOW(), NOW() FROM courses WHERE slug = 'english-it';

INSERT INTO lessons (course_id, slug, title, summary, content, video_url, position, duration_minutes, created_at, updated_at)
SELECT id, '3-english-pr', 'Pull Request және Code Review жазу', 'Командамен ағылшынша коммуникация',
'Дұрыс commit message жазу: feat, fix, docs, refactor префикстері. Pull Request сипаттамасын жазу: не өзгертілді, неліктен, қалай тестіленді. Code Review пікірлерін жазу және қабылдау. Slack/Teams-та командамен кәсіби коммуникация.',
'https://www.youtube.com/watch?v=oPpnCh7InLY', 3, 20, NOW(), NOW() FROM courses WHERE slug = 'english-it';
