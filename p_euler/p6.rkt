#lang racket

(define (sum-square-diff n)
  (let ([u (+ 1 n)])
    (define (times-range k)
      (- (foldl + 0 (map (lambda (i) (* k i))
                         (range 1 u)))
         (* k k)))
    (foldl + 0 (map times-range (range 1 u)))))

(sum-square-diff 10)
(sum-square-diff 100)