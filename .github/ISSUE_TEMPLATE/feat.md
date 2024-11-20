name: Feature
description: Add New Feature
labels: [feat]
title: '[Feat]:'
body:

- type: textarea
  id: description
  attributes:
  description: 구현할 기능을 설명해주세요.
  label: Description
  validations:
  required: true

- type: textarea
  id: possible-solution
  attributes:
  label: Possible Solution
  placeholder: I think this is probably...
  validations:
  required: false

- type: textarea
  id: etc
  attributes:
  label: etc.
  validations:
  required: false