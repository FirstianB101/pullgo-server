# pullgo-server [![Build Status](https://www.travis-ci.com/FirstianB101/pullgo-server.svg?branch=master)](https://www.travis-ci.com/FirstianB101/pullgo-server)

소규모 학원들을 위한 수업 관리와 시험 응시 플랫폼 'pullgo'의 백엔드 서버 프로젝트입니다.

## Project pullgo

프로젝트 'pullgo'는 자체적인 홈페이지를 갖지 않는 소규모 학원들을 위한 플랫폼 서비스입니다.

선생님은 학원을 개설하고, 그 속에서 반을 운영하며, 정기적으로 수업을 생성할 수 있습니다. 다른 선생님은 학원 혹은 반에 가입 요청을 보내 소속될 수 있습니다.

학생은 학원에 등록하고, 소속된 반에서 수업을 확인하며, 시험에 응시할 수 있습니다. 학원 혹은 반에 가입하기 위해서는 선생님이 가입 요청을 승인해야 합니다.

선생님은 수업에서 시험을 생성할 수 있습니다. 시험은 일반적인 온라인 시험과 같이 시작 시간과 종료 시간, 시간 제한이 설정됩니다. 시험 생성 시 새로운 문제를 작성하거나, 기존에
냈던 문제를 복사해서 시험에 낼 수 있습니다. 시험 설정에 따라 답안 제출 시 즉시 채점하거나 시험 종료 시 일괄적으로 채점할 수 있습니다. 시험이 종료되고 채점이 끝나면 학생의
부모 전화번호로 그 결과를 문자로 전송하는 기능도 지원됩니다.

공부에 주로 휴대폰을 사용하는 학생들을 위해 안드로이드와 iOS 앱을 지원하고, 학원 관리같이 복잡한 작업이 필요한 선생님들을 위해 웹 인터페이스 또한 지원합니다.

### Links

* 웹 페이지: 준비 중
* Android: [Repo (private)](https://github.com/FirstianB101/pullgo-android)
* iOS: [Repo (private)](https://github.com/FirstianB101/pullgo-ios)

## Documentation

* REST API 가이드: https://api.pullgo.kr/v1/docs/api-guide.html

## Issues

* 문제가 생겼다면 [Issue Tracker](https://github.com/FirstianB101/pullgo-server/issues)에 문의해주세요.

## Contribution

* [Pull Requests](https://github.com/FirstianB101/pullgo-server/pulls)를 통해 기여해주세요.
* Code style: [GoogleStyle 기반의 커스텀 스타일](./GoogleStyle%20(pullgo-server).xml)