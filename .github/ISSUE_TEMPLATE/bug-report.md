name: Bug Report
description: Report a bug
labels: [bug]
title: '[Bug]:'
body:

- type: textarea
  id: description
  attributes:
  description: 버그 사항 기술
  label: Bug description
  validations:
  required: true

- type: textarea
  id: expected
  attributes:
  label: 기대되는 결과

- type: textarea
  id: reproduction-steps
  attributes:
  label: To Reproduce
  description: 재현 방법
  placeholder: |
  최소한으로 재현 가능한 코드 또는 재현 단계를 설명해주세요. 선택 사항이지만 권장됩니다.

- type: textarea
  id: possible-solution
  attributes:
  label: 제안 솔루션
  placeholder: I think this is probably...
  validations:
  required: false

- type: textarea
  id: etc
  attributes:
  label: etc.
  validations:
  required: false