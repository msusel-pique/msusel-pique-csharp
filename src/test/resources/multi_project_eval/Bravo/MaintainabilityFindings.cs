using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bravo
{

    /// <summary>
    /// Expected Finidngs:
    ///     - CA1500: Variable names should not match field names
    /// </summary>
    class MaintainabilityFindings
    {
        int someField;

        void SomeMethodOne(int someField) { }
        void SomeMethodTwo()
        {
            int someField;
        }
    }
}
