/**
* File main.js V5 cho AloTra
* Khởi tạo tất cả thư viện (AOS, Swiper, Glightbox, PureCounter)
*/
(function() {
  "use strict";

  /**
   * Khởi tạo AOS (Animate On Scroll)
   */
  AOS.init({
    duration: 800, // Thời gian hiệu ứng
    easing: 'ease-in-out',
    once: true, // Chỉ chạy hiệu ứng 1 lần
    mirror: false
  });

  /**
   * Khởi tạo Swiper cho Hero Slider (Trang chủ)
   */
  const heroSlider = document.querySelector('.hero-slider');
  if (heroSlider) {
    new Swiper('.hero-slider', {
      loop: true,
      autoplay: {
        delay: 5000,
        disableOnInteraction: false
      },
      pagination: {
        el: '.swiper-pagination',
        clickable: true
      },
      navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev'
      },
      effect: 'fade', // Hiệu ứng mờ dần
      fadeEffect: {
        crossFade: true
      }
    });
  }

  /**
   * KhởiD tạo Swiper cho Testimonials (Cảm nhận)
   */
  const testimonialsSlider = document.querySelector('.testimonials-slider');
  if (testimonialsSlider) {
    new Swiper('.testimonials-slider', {
      loop: true,
      autoplay: {
        delay: 6000,
      },
      pagination: {
        el: '.swiper-pagination',
        clickable: true
      },
      breakpoints: {
        0: { slidesPerView: 1, spaceBetween: 20 },
        768: { slidesPerView: 2, spaceBetween: 30 },
        1200: { slidesPerView: 3, spaceBetween: 30 }
      }
    });
  }
  
  /**
   * Khởi tạo PureCounter (Đếm số)
   */
  const counters = document.querySelectorAll('.purecounter');
  if (counters.length > 0) {
     new PureCounter();
  }
  
  /**
   * Khởi tạo GLightbox (Xem ảnh)
   */
  const lightbox = GLightbox({
    selector: '.glightbox'
  });

  /**
   * Xử lý nút Scroll to Top
   */
  const scrollTop = document.querySelector('.scroll-top');
  if (scrollTop) {
    const toggleScrollTop = function() {
      window.scrollY > 100 ? scrollTop.classList.add('active') : scrollTop.classList.remove('active');
    }
    window.addEventListener('load', toggleScrollTop);
    document.addEventListener('scroll', toggleScrollTop);
    scrollTop.addEventListener('click', (e) => {
      e.preventDefault();
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    });
  }

})();

