# frozen_string_literal: false

# Ruby 2.4 preliminary compatibility script, loaded after all (2.3) jruby-openssl files

module OpenSSL

  module SSL
    class SSLContext
      # OpenSSL 1.1.0 introduced "security level"
      def security_level; 0 end
      def security_level=(level); raise NotImplementedError end
    end
  end

  module PKey

    class DH

      def set_key(pub_key, priv_key)
        self.public_key = pub_key
        self.priv_key = priv_key
        self
      end

      def set_pqg(p, q, g)
        self.p = p
        # TODO self.q = q
        if respond_to?(:q)
          self.q = q
        else
          OpenSSL.warn "JRuby-OpenSSL does not support setting q param on #{inspect}" if q
        end
        self.g = g
        self
      end

    end

    class DSA

      def set_key(pub_key, priv_key)
        self.public_key = pub_key
        self.priv_key = priv_key
        self
      end

      def set_pqg(p, q, g)
        self.p = p
        self.q = q
        self.g = g
        self
      end

    end

    class RSA

      def set_key(n, e, d)
        self.n = n
        self.e = e
        self.d = d
        self
      end

      def set_factors(p, q)
        self.p = p
        self.q = q
        self
      end

      def set_crt_params(dmp1, dmq1, iqmp)
        self.dmp1 = dmp1
        self.dmq1 = dmq1
        self.iqmp = iqmp
        self
      end

    end

    # openssl/lib/openssl/pkey.rb :

    class DH

      remove_const :DEFAULT_512 if const_defined?(:DEFAULT_512)

      DEFAULT_2048 = new <<-_end_of_pem_
-----BEGIN DH PARAMETERS-----
MIIBCAKCAQEA7E6kBrYiyvmKAMzQ7i8WvwVk9Y/+f8S7sCTN712KkK3cqd1jhJDY
JbrYeNV3kUIKhPxWHhObHKpD1R84UpL+s2b55+iMd6GmL7OYmNIT/FccKhTcveab
VBmZT86BZKYyf45hUF9FOuUM9xPzuK3Vd8oJQvfYMCd7LPC0taAEljQLR4Edf8E6
YoaOffgTf5qxiwkjnlVZQc3whgnEt9FpVMvQ9eknyeGB5KHfayAc3+hUAvI3/Cr3
1bNveX5wInh5GDx1FGhKBZ+s1H+aedudCm7sCgRwv8lKWYGiHzObSma8A86KG+MD
7Lo5JquQ3DlBodj3IDyPrxIv96lvRPFtAwIBAg==
-----END DH PARAMETERS-----
      _end_of_pem_

    end

    remove_const :DEFAULT_TMP_DH_CALLBACK if const_defined?(:DEFAULT_TMP_DH_CALLBACK)

    DEFAULT_TMP_DH_CALLBACK = lambda { |ctx, is_export, keylen|
      warn "using default DH parameters." if $VERBOSE
      case keylen
        when 1024 then OpenSSL::PKey::DH::DEFAULT_1024
        when 2048 then OpenSSL::PKey::DH::DEFAULT_2048
        else nil
      end
    }

  end

end
