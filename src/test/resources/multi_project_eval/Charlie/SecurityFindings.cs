using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Charlie
{
    /// <summary>
    /// Expected findings:
    ///     - CA2104: Do not declare read only mutable reference types
    /// </summary>
    public class SecurityFindings
    {
        static protected readonly StringBuilder SomeStringBuilder;

        /// <summary>
        /// Causes CA2014 to trigger
        /// </summary>
        static SecurityFindings()
        {
            SomeStringBuilder = new StringBuilder();
        }
    }
}
