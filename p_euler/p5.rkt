#lang racket

(require "number.rkt")

;;; Smallest multiple
;;;
;;; 2520 is the smallest number that can be divided by each of the
;;; numbers from 1 to 10 without any remainder.
;;; What is the smallest positive number that is evenly divisible by all of the numbers from 1 to 20?

(define (smallest-common-multiple n)
  (foldl lcm 1 (range 1 n)))

(smallest-common-multiple 10)
(smallest-common-multiple 20)