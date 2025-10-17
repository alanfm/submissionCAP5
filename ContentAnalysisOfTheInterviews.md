# Content Analysis of the Interviews

This section presents the complete content analysis of the interviews, systematized according to the technique by **Bardin (2016)**. The participants’ statements were segmented into recording units, each associated with a code and a thematic category that reflects the identified code quality criteria (readability, maintainability, modularity, standardization, functionality, and simplicity). For each unit, literal excerpts of statements are presented, followed by an analytical interpretation, allowing an understanding of how participants justified their choices and which quality dimensions were most valued. This organization provides methodological transparency and reinforces the validity of the results.

---

## Table – Content Analysis of the Interviews

| **Recording Unit** | **Code** | **Category** | **Example Statement** | **Interpretation** |
|--------------------|-----------|---------------|------------------------|--------------------|
| Readability as a central criterion (E6) | LEGIB | Readability | “*Code must first be readable; the more readable, the better.*” (E6) | Readability appears as the structural axis of quality assessment. |
| Balance between size and clarity (E1) | LEGIB | Readability | “*I like short but readable code; neither too summarized nor too long.*” (E1) | Readability is associated with appropriate size and immediate comprehension. |
| Ease of visual understanding (E4) | LEGIB | Readability | “*The version on the right is calmer, easier to understand.*” (E4) | Clarity of structure and fluent reading favor positive judgment. |
| Readability + standardization (E5) | LEGIB | Readability | “*Good code is readable and standardized; code 1 has many things out of standard.*” (E5) | Readability is reinforced by stable writing conventions. |
| Concise and clear text (E3) | LEGIB | Readability | “*Code 1 is concise and well summarized; code 2 is huge and not very readable.*” (E3) | Shorter and more direct code is perceived as more readable. |
| Readability through modularity (E2) | LEGIB | Readability | “*Code 2 is more modular, responsibilities divided, more readable.*” (E2) | Separation of responsibilities promotes readability. |
| Reading and maintenance (E7) | LEGIB | Readability | “*Quality code must be easy to read and maintain.*” (E7) | Readability is articulated with maintainability as a key quality criterion. |

---

## Maintainability and Modularity

| **Recording Unit** | **Code** | **Category** | **Example Statement** | **Interpretation** |
|--------------------|-----------|---------------|------------------------|--------------------|
| Small and better in the long run (E6) | MANUT | Maintainability | “*Code 2, being shorter, is better for analysis and long-term maintenance.*” (E6) | Moderate size reduces future maintenance effort. |
| Easier testing and maintenance (E1) | MANUT | Maintainability | “*In the second one, it’s easier to modularize and test.*” (E1) | A clearer structure facilitates evolution and correction. |
| Maintenance over the years (E4) | MANUT | Maintainability | “*Quality is reflected when I can maintain it over the years.*” (E4) | Quality criterion linked to an extended life cycle. |
| Dynamism and scalability (E5) | MANUT | Maintainability | “*If it’s not dynamic and scalable, the code degrades quickly.*” (E5) | Maintenance depends on architecture designed for evolution. |
| Compact and easy to modify (E3) | MANUT | Maintainability | “*Code 1 is compact and easy to maintain.*” (E3) | Concise structures simplify everyday maintenance. |
| Modularity helps maintenance (E2) | MANUT | Maintainability | “*Code 2 would be easier in the long term; class division helps.*” (E2) | Responsibility separation reduces coupling and change costs. |
| Clear logic and responsibilities (E7) | MANUT | Maintainability | “*Clear responsibilities and non-complex logic make maintenance easier.*” (E7) | Good internal organization facilitates future interventions. |
| Separation into classes (E6) | MODUL | Modularity | “*Separated into different classes, each with its own responsibility.*” (E6) | Modularity as a mechanism for controlling complexity. |
| OOP and class organization (E1) | MODUL | Modularity | “*I liked code 2 because it used multiple classes (object-oriented).*” (E1) | Distribution of responsibilities improves understanding. |
| Interfaces and maintenance (E4) | MODUL | Modularity | “*The use of interfaces improves maintainability.*” (E4) | Explicit contracts stabilize integrations and evolution. |
| MVC architecture (E5) | MODUL | Modularity | “*Focus on keeping controllers, views, and models separated.*” (E5) | Architectural patterns structure modules and layers. |
| Organized blocks (E3) | MODUL | Modularity | “*Code 1 is well separated into blocks, readable.*” (E3) | Segmentation into blocks contributes to local reasoning. |
| More classes, less coupling (E2) | MODUL | Modularity | “*Code 2 is well divided, more classes, separate responsibilities.*” (E2) | Modularity reduces coupling and facilitates reuse. |
| Modularization and reuse (E7) | MODUL | Modularity | “*Responsibilities modularized into smaller classes make reuse easier.*” (E7) | Componentization encourages reuse and incremental evolution. |

---

## Standardization, Functionality, and Simplicity

| **Recording Unit** | **Code** | **Category** | **Example Statement** | **Interpretation** |
|--------------------|-----------|---------------|------------------------|--------------------|
| Writing standards (E5) | PADR | Naming | “*Good code is readable and standardized; there are things out of standard.*” (E5) | Adherence to conventions improves team cohesion. |
| Descriptive names (E7) | PADR | Naming | “*Naming variables and functions well.*” (E7) | Clear naming reduces ambiguity and reading costs. |
| Observance of conventions (E6) | PADR | Naming | “*Well-written code follows standards; without them, it’s difficult.*” (E6) | Consistent style as a sign of maturity and quality. |
| Meeting requirements (E1) | FUNC | Functionality | “*Code must be functional.*” (E1) | Proper functioning is a minimal quality requirement. |
| Function before aesthetics (E5) | FUNC | Functionality | “*It’s no use being pretty; it must work.*” (E5) | Priority is given to correct behavior over appearance. |
| Functional and sustainable (E7) | FUNC | Functionality | “*Quality code is functional, easy to read, and maintain.*” (E7) | Functionality integrated with readability and maintainability. |
| Preference for concise solutions (E6) | SIMP | Simplicity | “*I prefer concise and clear code.*” (E6) | Removal of accidental complexity and focus on the essential. |
| Avoiding excessive length (E1) | SIMP | Simplicity | “*Short and readable; neither too summarized nor too long.*” (E1) | Simplicity as a balance between brevity and clarity. |
| Critique of large methods (E2) | SIMP | Simplicity | “*Huge methods and too many responsibilities; hard to maintain.*” (E2) | Indication of a need for decomposition/refactoring. |

---

## Additional Categories

| **Recording Unit** | **Code** | **Category** | **Example Statement** | **Interpretation** |
|--------------------|-----------|---------------|------------------------|--------------------|
| Spontaneous clarity criterion (E6) | ESPONT | Spontaneous Criteria | “*I prefer code that anyone can look at and understand.*” (E6) | Readability spontaneously mentioned as a universal priority. |
| Spontaneous efficiency criterion (E2) | ESPONT | Spontaneous Criteria | “*Clear code helps, but it also has to be efficient.*” (E2) | Efficiency appears linked to clarity judgment. |
| Choice for concise code (E3) | PREF | Preference | “*There’s no comparison; code 1 is concise, perfect, well summarized.*” (E3) | Clear preference for the more compact code. |
| Preference for modularity (E2) | PREF | Preference | “*Code 2 is much more modular, divided responsibilities.*” (E2) | Option motivated by responsibility separation. |
| Justification by readability (E4) | JUST | Justification | “*The one on the right is calmer, easier to understand.*” (E4) | Choice based on ease of reading. |
| Justification by standardization (E5) | JUST | Justification | “*Code 1 has many things out of standard.*” (E5) | Standardization criterion guides the decision. |
| AI as a support tool (E3) | IAPOS | Post-Revelation | “*I believe it will be a tool, right? To assist. I think that’s it.*” (E3) | Positive perception, reinforcing the complementary role of AI. |
| Risk in production use (E7) | IAPOS | Post-Revelation | “*Of course, I’d avoid using LLM directly in production code. I think code review is necessary.*” (E7) | Emphasis on the need for human supervision. |
| AI as a productivity catalyst (E4) | FUT | Future Expectations | “*It will make things more fluid [...] but ending the developer’s work, I don’t think so.*” (E4) | Expectation of productivity gains without full replacement. |
| Inevitable integration (E7) | FUT | Future Expectations | “*If you’re not using LLM, you’re falling behind. You’re missing an opportunity.*” (E7) | Expectation of growing adoption as a competitive advantage. |

---