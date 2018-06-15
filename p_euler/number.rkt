#lang racket

(provide gcd lcm do-sieve! next-prime)

;; calculate the greatest common divisor for a and b
;; e.g.
;; (gcd 4 6) => 2
(define (gcd a b)
  (if (zero? b)
      a
      (gcd b (remainder a b))))


;; calculates the least common multiple of a and b
;; e.g.
;; (lcm 4 6) => 12
(define (lcm a b)
  (/ (* a b)
     (gcd a b)))


;; removes all multples of p in vector v except p
(define (remove-all-multiples! v p)
  (for ([i (in-range (+ p p) (vector-length v) p)])
    (vector-set! v i false))
  v)


;; returns the next prime greater than or equals to n using prime vector v
(define (next-prime v n)
  (let ([v-len (vector-length v)])
    (define (helper k)
      (cond ((>= k v-len) -1)
            ((vector-ref v k) k)
            (else (helper (+ 1 k)))))
    (helper n)))


;; runs the sieve of eratosthene
;; sample usage:
;;
;; (do-sieve! (make-vector 10 true))
;;    => '#(#f #f #t #t #f #t #f #t #f #f)
;;
(define (do-sieve! vec)
  (let ([max-v (vector-length vec)])
    (define (helper k)
      (if (>= k max-v)
          vec
          (let ([next-p (next-prime (remove-all-multiples! vec k)
                                    (+ 1 k))])
            (if (eqv? -1 next-p)
                vec
                (helper next-p)))))
    (vector-set! vec 0 false)
    (vector-set! vec 1 false)
    (helper 2)))
