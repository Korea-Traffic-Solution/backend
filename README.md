## 📠 Convention

### 🤝 Branch Naming Convention

| 머릿말  | 설명                               |
| ------- | ---------------------------------- |
| main    | 서비스 브랜치                      |
| develop | 배포 전 작업 기준                  |
| feat | 기능 단위 구현                     |
| hotfix  | 서비스 중 긴급 수정 건에 대한 처리 |
 fix        | 버그 수정 및 에러 해결 작업                      |
| refactor   | 코드 리팩토링 (기능 변경 없이 코드 구조 개선)    |

<details>
<summary>Branch Naming Convention Detail</summary>
<div markdown="1">

```
master(main) ── develop ── feature
└── hotfix                └── fix 
                          └── refactor 
```

- [ ] [깃 플로우](https://techblog.woowahan.com/2553/)를 베이스로 하여 프로젝트 사이즈에 맞게 재정의했습니다.
- [ ] 브랜치 이름은 `kebab-case`를 따릅니다.
- [ ] 이슈 번호는 가장 마지막에 적습니다. (ex. #_)

#### master(main)

- [ ] 실제 서비스가 이루어지는 브랜치입니다.
- [ ] 이 브랜치를 기준으로 develop 브랜치가 분기됩니다.
- [ ] 배포 중, 긴급하게 수정할 건이 생길시 hotfix 브랜치를 만들어 수정합니다.

#### develop

- [ ] 개발, 테스트, 릴리즈 등 배포 전 작업의 기준이 되는 브랜치입니다.
- [ ] 해당 브랜치를 default로 설정합니다.
- [ ] 이 브랜치에서 feature 브랜치가 분기됩니다.

#### feature

- [ ] 개별 개발자가 맡은 작업을 개발하는 브랜치입니다.
- [ ] `feat/(feat-name)` 과 같이 머릿말을 feat, 꼬릿말을 개발하는 기능으로 명명합니다.
- [ ] feat-name의 경우 kebab-case를 따릅니다.
- [ ] ex) feat/social-login-#5

#### fix

- [ ] 버그나 에러를 수정하는 브랜치입니다.
- [ ] `fix/(수정내용)` 형식으로 명명합니다.
- [ ] ex) `fix/login-error-#8`

---

#### refactor

- [ ] 코드 구조를 개선하거나 리팩토링하는 브랜치입니다. (기능 변화 없음)
- [ ] `refactor/(개선내용)` 형식으로 명명합니다.
- [ ] ex) `refactor/remove-duplication-#12`

#### hotfix

- [ ] 서비스 중 긴급히 수정해야 할 사항이 발생할 때 사용합니다.
- [ ] main에서 분기됩니다.

</div>
</details>

### 🤝 Commit Convention

| 머릿말           | 설명                                                                      |
| ---------------- | ------------------------------------------------------------------------- |
| Feat             | 새로운 기능 추가                                                          |
| Fix              | 버그 수정                                                                 |
| Refactor         | 코드 리팩토링                                                  |
| Style         | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우                                                  |
| Comment          | 필요한 주석 추가 및 변경                                                  |
| Docs             | 문서 수정                                                                 |
| Test             | 테스트 코드, 리팩토링 테스트 코드 추가                        |
| Chore            | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| Rename           | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우                        |
| Remove           | 파일을 삭제하는 작업만 수행한 경우                                        |
| !BREAKING CHANGE | 커다란 API 변경의 경우                                                    |
| !HOTFIX          | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우                     |

<details>
<summary>Commit Convention Detail</summary>
<div markdown="1">

### 1. 제목과 본문을 빈행으로 분리

- 커밋 유형 이후 제목과 본문은 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
- 본문에는 변경한 내용과 이유 설명 (어떻게보다는 무엇 & 왜를 설명)

### 2. 제목 첫 글자는 대문자로, 끝에는 `.` 금지

### 3. 제목은 영문 기준 50자 이내로 할 것

### 4. 마지막에 이슈번호 추가하기

### 5. 자신의 코드가 직관적으로 바로 파악할 수 있다고 생각하지 말자

### 6. 여러가지 항목이 있다면 글머리 기호를 통해 가독성 높이기

```
- 변경 내용 1
- 변경 내용 2
- 변경 내용 3
```

### 8. 예시
커밋유형: 기능 설명 (#이슈번호)
ex) Feat: 로그인 기능 구현 (#5)

</div>
</details>
